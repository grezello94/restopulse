package com.redlantern.restopulse.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redlantern.restopulse.data.repository.BackupRepository
import com.redlantern.restopulse.data.repository.ExportRepository
import com.redlantern.restopulse.models.ExportFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exports: ExportRepository,
    private val backups: BackupRepository
) : ViewModel() {
    private val _shareIntent = MutableSharedFlow<Intent>()
    val shareIntent = _shareIntent.asSharedFlow()

    fun export(format: ExportFormat) = viewModelScope.launch {
        _shareIntent.emit(exports.export(format))
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
