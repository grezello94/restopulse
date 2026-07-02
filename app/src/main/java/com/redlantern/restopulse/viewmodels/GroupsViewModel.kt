package com.redlantern.restopulse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.database.entities.MarketingGroupEntity
import com.redlantern.restopulse.data.repository.MarketingGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val repository: MarketingGroupRepository
) : ViewModel() {
    val groups: StateFlow<List<MarketingGroupEntity>> = repository.observeGroups().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun rename(group: MarketingGroupEntity, name: String) = viewModelScope.launch { repository.rename(group, name) }
}
