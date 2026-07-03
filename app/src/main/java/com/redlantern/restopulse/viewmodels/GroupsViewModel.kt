package com.redlantern.restopulse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.database.entities.MarketingGroupEntity
import com.redlantern.restopulse.data.repository.MarketingGroupRepository
import com.redlantern.restopulse.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val repository: MarketingGroupRepository,
    private val customers: CustomerRepository
) : ViewModel() {
    val groups: StateFlow<List<MarketingGroupEntity>> = repository.observeGroups().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun prepareBatches() = viewModelScope.launch {
        runCatching {
            val imported = customers.importPhoneBook()
            val assigned = repository.prepareBroadcastBatches()
            imported to assigned
        }
            .onSuccess { (imported, assigned) ->
                _message.value = when {
                    imported == 0 && assigned == 0 -> "Everyone is already assigned once"
                    else -> "$imported contacts imported • $assigned assigned without overlap"
                }
            }
            .onFailure { _message.value = it.message ?: "Could not prepare batches" }
    }

    fun clearMessage() { _message.value = null }
    fun rename(group: MarketingGroupEntity, name: String) = viewModelScope.launch { repository.rename(group, name) }
}
