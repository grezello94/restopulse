package com.redlantern.restopulse.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.redlantern.restopulse.workers.WorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var scheduler: WorkScheduler
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduler.schedulePeriodicSync()
            scheduler.enqueueInitialImport()
        }
    }
}
