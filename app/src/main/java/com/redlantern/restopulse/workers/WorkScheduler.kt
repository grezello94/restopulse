package com.redlantern.restopulse.workers

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    fun enqueueInitialImport() {
        workManager.enqueueUniqueWork(
            "initial-call-import",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<InitialImportWorker>().build()
        )
    }

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "call-log-sync",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<CallLogSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
        )
    }

    fun enqueueLatestSync() {
        workManager.enqueue(OneTimeWorkRequestBuilder<CallLogSyncWorker>().build())
    }
}
