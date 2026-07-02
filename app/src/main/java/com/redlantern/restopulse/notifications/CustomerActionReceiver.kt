package com.redlantern.restopulse.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.redlantern.restopulse.data.database.entities.CallHistoryEntity
import com.redlantern.restopulse.data.repository.CustomerRepository
import com.redlantern.restopulse.domain.CustomerUseCases
import com.redlantern.restopulse.models.CallType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomerActionReceiver : BroadcastReceiver() {
    @Inject lateinit var customers: CustomerRepository
    @Inject lateinit var customerUseCases: CustomerUseCases
    @Inject lateinit var notifications: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val number = intent.getStringExtra(NotificationHelper.EXTRA_NUMBER).orEmpty()
        val normalized = intent.getStringExtra(NotificationHelper.EXTRA_NORMALIZED).orEmpty()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    NotificationHelper.ACTION_ADD -> {
                        val id = customerUseCases.addDiscoveredCustomer(
                            CallHistoryEntity(
                                customerId = null,
                                phoneNumber = number,
                                normalizedNumber = normalized,
                                callerName = "",
                                callType = CallType.UNKNOWN,
                                callDate = System.currentTimeMillis(),
                                durationSeconds = 0,
                                savedToCustomer = false
                            )
                        )
                        if (id > 0) {
                            notifications.showSimple("Customer Added", "$number was added to RestoPulse")
                        } else {
                            notifications.showSimple("Duplicate Prevented", "$number already exists")
                        }
                    }
                    NotificationHelper.ACTION_IGNORE -> {
                        customers.ignore(number, normalized)
                        notifications.showSimple("Customer Ignored", "$number will not be suggested again")
                    }
                    NotificationHelper.ACTION_LATER -> notifications.showSimple("Saved for Later", "$number remains in recent calls")
                }
            } finally {
                pending.finish()
            }
        }
    }
}
