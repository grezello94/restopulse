package com.redlantern.restopulse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.database.entities.CustomerEntity
import com.redlantern.restopulse.data.repository.CustomerRepository
import com.redlantern.restopulse.models.CustomerFilter
import com.redlantern.restopulse.utils.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val repository: CustomerRepository,
    private val dateTime: DateTimeUtils
) : ViewModel() {
    val query = MutableStateFlow("")
    val filter = MutableStateFlow(CustomerFilter.ALL)

    val customers: StateFlow<List<CustomerEntity>> = combine(
        query
            .debounce(200)
            .map(String::trim)
            .distinctUntilChanged()
            .flatMapLatest { repository.search(it) },
        filter
    ) { list, activeFilter -> list.filterBy(activeFilter) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun List<CustomerEntity>.filterBy(filter: CustomerFilter): List<CustomerEntity> {
        val now = System.currentTimeMillis()
        return when (filter) {
            CustomerFilter.ALL -> this
            CustomerFilter.WHATSAPP -> filter { it.whatsappAvailable }
            CustomerFilter.NON_WHATSAPP -> filter { !it.whatsappAvailable }
            CustomerFilter.VIP -> filter { it.vip }
            CustomerFilter.FAVORITES -> filter { it.favorite }
            CustomerFilter.TODAY -> filter { it.lastCallDate >= dateTime.startOfToday() }
            CustomerFilter.WEEKLY -> filter { it.lastCallDate >= now - 7L * 24 * 60 * 60 * 1000 }
            CustomerFilter.MONTHLY -> filter { it.lastCallDate >= now - 30L * 24 * 60 * 60 * 1000 }
            CustomerFilter.MISSED -> filter { it.missedCount > 0 }
            CustomerFilter.INCOMING -> filter { it.incomingCount > 0 }
            CustomerFilter.OUTGOING -> filter { it.outgoingCount > 0 }
        }
    }
}
