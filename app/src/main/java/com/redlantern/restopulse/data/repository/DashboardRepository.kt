package com.redlantern.restopulse.data.repository

import com.redlantern.restopulse.data.preferences.SettingsRepository
import com.redlantern.restopulse.domain.DashboardStats
import com.redlantern.restopulse.utils.DateTimeUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart

@Singleton
class DashboardRepository @Inject constructor(
    customers: CustomerRepository,
    calls: CallRepository,
    groups: MarketingGroupRepository,
    settings: SettingsRepository,
    dateTime: DateTimeUtils
) {
    private val numeric = combine(
        customers.observeCount().onStart { emit(0) }.catch { emit(0) },
        customers.observeNewSince(dateTime.startOfToday()).onStart { emit(0) }.catch { emit(0) },
        calls.observeCallsSince(dateTime.startOfToday()).onStart { emit(0) }.catch { emit(0) },
        customers.observeWhatsappCount().onStart { emit(0) }.catch { emit(0) },
        customers.observeNonWhatsappCount().onStart { emit(0) }.catch { emit(0) }
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
        customers.observeDuplicateCount().onStart { emit(0) }.catch { emit(0) },
        groups.observeGroups().map { it.size }.onStart { emit(0) }.catch { emit(0) },
        customers.observeFrequentCount().onStart { emit(0) }.catch { emit(0) },
        settings.settings.map { it.lastExportStatus }.onStart { emit("Ready") }.catch { emit("Ready") }
    ) { base, duplicates, groupCount, frequentCount, exportStatus ->
        base.copy(
            duplicatesPrevented = duplicates,
            marketingGroups = groupCount,
            frequentCustomers = frequentCount,
            exportStatus = exportStatus
        )
    }
}
