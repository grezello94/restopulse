package com.redlantern.restopulse.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.database.entities.CustomerEntity
import com.redlantern.restopulse.data.repository.MarketingGroupRepository
import com.redlantern.restopulse.data.services.WhatsAppChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class BroadcastBatchViewModel @Inject constructor(
    repository: MarketingGroupRepository,
    private val whatsAppChecker: WhatsAppChecker,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val groupId: Long = checkNotNull(savedStateHandle["groupId"])

    val members: StateFlow<List<CustomerEntity>> = repository.observeMembers(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun openWhatsApp(customer: CustomerEntity) = whatsAppChecker.openChat(customer.normalizedNumber)
}
