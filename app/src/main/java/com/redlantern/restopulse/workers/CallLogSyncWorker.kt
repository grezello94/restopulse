package com.redlantern.restopulse.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.redlantern.restopulse.data.repository.CallRepository
import com.redlantern.restopulse.data.repository.CustomerRepository
import com.redlantern.restopulse.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CallLogSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val calls: CallRepository,
    private val customers: CustomerRepository,
    private val notifications: NotificationHelper
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        val latest = calls.importLatest()
        if (latest != null) {
            // Contact-provider failures are isolated so normal call sync remains healthy.
            runCatching { calls.promoteIfNowFrequent(latest.normalizedNumber) }
        }
        if (latest != null &&
            customers.findByNormalized(latest.normalizedNumber) == null &&
            !customers.isAndroidContact(latest.phoneNumber) &&
            !customers.isIgnored(latest.normalizedNumber)
        ) {
            notifications.showNewCustomer(latest.phoneNumber, latest.normalizedNumber)
        }
        Result.success()
    }.getOrElse { Result.retry() }
}
