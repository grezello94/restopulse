package com.redlantern.restopulse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.repository.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface InitialImportState {
    data object Idle : InitialImportState
    data object Importing : InitialImportState
    data object Complete : InitialImportState
    data class Failed(val message: String) : InitialImportState
}

@HiltViewModel
class InitialImportViewModel @Inject constructor(
    private val calls: CallRepository
) : ViewModel() {
    private val _state = MutableStateFlow<InitialImportState>(InitialImportState.Idle)
    val state: StateFlow<InitialImportState> = _state.asStateFlow()

    fun startImport() {
        if (_state.value == InitialImportState.Importing || _state.value == InitialImportState.Complete) return
        viewModelScope.launch {
            _state.value = InitialImportState.Importing
            _state.value = runCatching {
                withContext(Dispatchers.IO) {
                    calls.importAllAndCreateContacts()
                }
                InitialImportState.Complete
            }.getOrElse { InitialImportState.Failed(it.message ?: it.javaClass.simpleName) }
        }
    }
}
