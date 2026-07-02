package com.redlantern.restopulse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.database.entities.CallHistoryEntity
import com.redlantern.restopulse.data.repository.CallRepository
import com.redlantern.restopulse.models.CallSort
import com.redlantern.restopulse.models.CallType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CallsViewModel @Inject constructor(
    calls: CallRepository
) : ViewModel() {
    val sort = MutableStateFlow(CallSort.NEWEST)
    val calls: StateFlow<List<CallHistoryEntity>> = combine(calls.observeRecent(), sort) { list, sort ->
        when (sort) {
            CallSort.NEWEST -> list.sortedByDescending { it.callDate }
            CallSort.OLDEST -> list.sortedBy { it.callDate }
            CallSort.DURATION -> list.sortedByDescending { it.durationSeconds }
            CallSort.INCOMING -> list.filter { it.callType == CallType.INCOMING }
            CallSort.OUTGOING -> list.filter { it.callType == CallType.OUTGOING }
            CallSort.MISSED -> list.filter { it.callType == CallType.MISSED }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
