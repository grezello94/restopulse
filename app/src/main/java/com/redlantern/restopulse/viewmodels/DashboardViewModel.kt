package com.redlantern.restopulse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.database.entities.CallHistoryEntity
import com.redlantern.restopulse.data.repository.CallRepository
import com.redlantern.restopulse.data.repository.DashboardRepository
import com.redlantern.restopulse.domain.DashboardStats
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface DashboardScanState {
    data object Idle : DashboardScanState
    data object Scanning : DashboardScanState
    data object Complete : DashboardScanState
    data class Failed(val message: String) : DashboardScanState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    dashboard: DashboardRepository,
    private val calls: CallRepository
) : ViewModel() {
    val stats: StateFlow<DashboardStats> = dashboard.stats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardStats())
    val recentCalls: StateFlow<List<CallHistoryEntity>> = calls.observeRecent().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _scanState = MutableStateFlow<DashboardScanState>(DashboardScanState.Idle)
    val scanState: StateFlow<DashboardScanState> = _scanState.asStateFlow()

    init {
        startInitialScan()
    }

    fun startInitialScan() {
        if (_scanState.value == DashboardScanState.Scanning) return
        _scanState.value = DashboardScanState.Scanning
        viewModelScope.launch {
            _scanState.value = runCatching {
                withContext(Dispatchers.IO) {
                    calls.importAllAndCreateContacts()
                }
                DashboardScanState.Complete
            }.getOrElse { error ->
                DashboardScanState.Failed(error.message ?: error.javaClass.simpleName)
            }
        }
    }
}
