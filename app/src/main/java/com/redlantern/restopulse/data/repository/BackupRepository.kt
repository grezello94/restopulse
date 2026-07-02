package com.redlantern.restopulse.data.repository

import android.content.Context
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun backupUri(): android.net.Uri {
        val source = context.getDatabasePath("restopulse.db")
        val dir = File(context.cacheDir, "backups").apply { mkdirs() }
        val backup = File(dir, "restopulse-${System.currentTimeMillis()}.db")
        source.copyTo(backup, overwrite = true)
        return FileProvider.getUriForFile(context, "${context.packageName}.files", backup)
    }
}
