package com.redlantern.restopulse.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.redlantern.restopulse.data.repository.CallRepository
import com.redlantern.restopulse.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CallSyncForegroundService : Service() {
    @Inject lateinit var calls: CallRepository
    @Inject lateinit var notifications: NotificationHelper
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(42, notifications.syncNotification("Syncing recent calls"))
        scope.launch {
            calls.importRecent()
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
