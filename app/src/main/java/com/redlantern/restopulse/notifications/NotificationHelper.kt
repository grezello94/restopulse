package com.redlantern.restopulse.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.redlantern.restopulse.MainActivity
import com.redlantern.restopulse.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CUSTOMERS_CHANNEL, "Customer updates", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Unknown customer discovery and CRM updates" }
        )
        manager.createNotificationChannel(
            NotificationChannel(SYNC_CHANNEL, "Background sync", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Call log sync and export progress" }
        )
    }

    fun showNewCustomer(number: String, normalized: String) {
        if (!canNotify()) return
        val contentIntent = PendingIntent.getActivity(
            context,
            normalized.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CUSTOMERS_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("New Customer Found")
            .setContentText("Would you like to add $number?")
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "ADD", actionIntent(ACTION_ADD, number, normalized))
            .addAction(0, "IGNORE", actionIntent(ACTION_IGNORE, number, normalized))
            .addAction(0, "LATER", actionIntent(ACTION_LATER, number, normalized))
            .build()
        NotificationManagerCompat.from(context).notify(normalized.hashCode(), notification)
    }

    fun syncNotification(text: String) = NotificationCompat.Builder(context, SYNC_CHANNEL)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("RestoPulse")
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()

    fun showSimple(title: String, message: String) {
        if (!canNotify()) return
        val notification = NotificationCompat.Builder(context, CUSTOMERS_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify((title + message).hashCode(), notification)
    }

    private fun actionIntent(action: String, number: String, normalized: String): PendingIntent {
        val intent = Intent(context, CustomerActionReceiver::class.java)
            .setAction(action)
            .putExtra(EXTRA_NUMBER, number)
            .putExtra(EXTRA_NORMALIZED, normalized)
        return PendingIntent.getBroadcast(
            context,
            (action + normalized).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun canNotify(): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val CUSTOMERS_CHANNEL = "customers"
        const val SYNC_CHANNEL = "sync"
        const val ACTION_ADD = "com.redlantern.restopulse.ADD"
        const val ACTION_IGNORE = "com.redlantern.restopulse.IGNORE"
        const val ACTION_LATER = "com.redlantern.restopulse.LATER"
        const val EXTRA_NUMBER = "number"
        const val EXTRA_NORMALIZED = "normalized"
    }
}
