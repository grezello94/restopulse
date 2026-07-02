package com.redlantern.restopulse.data.repository

import com.redlantern.restopulse.data.preferences.SettingsRepository
import com.redlantern.restopulse.domain.DashboardStats
import com.redlantern.restopulse.utils.DateTimeUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

@Singleton
class DashboardRepository @Inject constructor(
    customers: CustomerRepository,
    calls: CallRepository,
    groups: MarketingGroupRepository,
    settings: SettingsRepository,
    dateTime: DateTimeUtils
) {
    private val numeric = combine(
        customers.observeCount(),
        customers.observeNewSince(dateTime.startOfToday()),
        calls.observeCallsSince(dateTime.startOfToday()),
        customers.observeWhatsappCount(),
        customers.observeNonWhatsappCount()
    ) { total, newToday, callsToday, whatsapp, nonWhatsapp ->
        DashboardStats(
            totalCustomers = total,
            newCustomersToday = newToday,
            callsToday = callsToday,
            whatsappCustomers = whatsapp,
            nonWhatsappCustomers = nonWhatsapp
        )
    }

    val stats: Flow<DashboardStats> = combine(
        numeric,
        customers.observeDuplicateCount(),
        groups.observeGroups().map { it.size },
        settings.settings.map { it.lastExportStatus }
    ) { base, duplicates, groupCount, exportStatus ->
        base.copy(
            duplicatesPrevented = duplicates,
            marketingGroups = groupCount,
            exportStatus = exportStatus
        )
    }
}
