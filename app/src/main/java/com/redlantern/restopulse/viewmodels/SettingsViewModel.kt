package com.redlantern.restopulse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.preferences.SettingsRepository
import com.redlantern.restopulse.data.repository.CallRepository
import com.redlantern.restopulse.domain.AppSettings
import com.redlantern.restopulse.domain.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val calls: CallRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { repository.setTheme(mode) }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { repository.setDynamicColor(enabled) }
    fun setMaxGroupSize(size: Int) = viewModelScope.launch { repository.setMaxGroupSize(size) }

    val frequentContactsStatus = MutableStateFlow<String?>(null)
    val organizingFrequentContacts = MutableStateFlow(false)

    fun organizeFrequentContacts() = viewModelScope.launch {
        if (organizingFrequentContacts.value) return@launch
        organizingFrequentContacts.value = true
        frequentContactsStatus.value = runCatching {
            withContext(Dispatchers.IO) {
                calls.organizeFrequentExistingContacts()
            }
        }.fold(
            onSuccess = { result ->
                "Formatted ${result.formattedExistingContacts} existing RL contacts and renamed ${result.renamedContacts} contacts from ${result.frequentNumbers} repeat callers. ${result.uniqueNumbersInLog} unique numbers checked."
            },
            onFailure = { error -> "Could not organize contacts: ${error.message ?: "unknown error"}" }
        )
        organizingFrequentContacts.value = false
    }
}
