package com.redlantern.restopulse.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.redlantern.restopulse.data.repository.CallRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class InitialImportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val calls: CallRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        calls.importAllAndCreateContacts()
        Result.success()
    }.getOrElse { Result.retry() }
}
