package com.redlantern.restopulse.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.database.entities.CallHistoryEntity
import com.redlantern.restopulse.data.database.entities.CustomerEntity
import com.redlantern.restopulse.data.repository.CallRepository
import com.redlantern.restopulse.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val customers: CustomerRepository,
    calls: CallRepository
) : ViewModel() {
    private val customerId: Long = checkNotNull(savedStateHandle.get<Long>("customerId"))
    val customer: StateFlow<CustomerEntity?> = customers.observeCustomer(customerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val history: StateFlow<List<CallHistoryEntity>> = flowOf(customerId)
        .flatMapLatest { calls.observeForCustomer(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(customer: CustomerEntity) = viewModelScope.launch { customers.save(customer) }
    fun addToAndroidContacts(customer: CustomerEntity) = viewModelScope.launch { customers.addToAndroidContacts(customer) }
}
