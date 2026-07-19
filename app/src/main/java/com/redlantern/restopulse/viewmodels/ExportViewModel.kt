package com.redlantern.restopulse.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.repository.BackupRepository
import com.redlantern.restopulse.data.repository.CustomerRepository
import com.redlantern.restopulse.data.repository.ExportRepository
import com.redlantern.restopulse.models.ExportFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exports: ExportRepository,
    private val backups: BackupRepository,
    customers: CustomerRepository
) : ViewModel() {
    private val _shareIntent = MutableSharedFlow<Intent>()
    val shareIntent = _shareIntent.asSharedFlow()

    private val _namePrefix = MutableStateFlow("RL")
    val namePrefix = _namePrefix.asStateFlow()

    val matchingCount = combine(customers.observeCustomers(), _namePrefix) { list, rawPrefix ->
        val prefix = rawPrefix.trim()
        list.count { prefix.isBlank() || it.name.trimStart().startsWith(prefix, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setNamePrefix(value: String) {
        _namePrefix.value = value
    }

    fun export(format: ExportFormat) = viewModelScope.launch {
        _shareIntent.emit(exports.export(format))
    }

    fun exportMatchingExcel() = viewModelScope.launch {
        _shareIntent.emit(exports.export(ExportFormat.EXCEL, _namePrefix.value))
    }

    fun backup() = viewModelScope.launch {
        val uri = backups.backupUri()
        _shareIntent.emit(
            Intent(Intent.ACTION_SEND)
                .setType("application/octet-stream")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }
}
