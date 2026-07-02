package com.redlantern.restopulse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.database.entities.CallHistoryEntity
import com.redlantern.restopulse.data.repository.CallRepository
import com.redlantern.restopulse.data.repository.DashboardRepository
import com.redlantern.restopulse.domain.DashboardStats
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(
    dashboard: DashboardRepository,
    calls: CallRepository
) : ViewModel() {
    val stats: StateFlow<DashboardStats> = dashboard.stats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardStats())
    val recentCalls: StateFlow<List<CallHistoryEntity>> = calls.observeRecent().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
