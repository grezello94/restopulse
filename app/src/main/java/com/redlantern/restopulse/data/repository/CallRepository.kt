package com.redlantern.restopulse.data.repository

import android.util.Log
import com.redlantern.restopulse.data.database.dao.CallHistoryDao
import com.redlantern.restopulse.data.database.entities.CallHistoryEntity
import com.redlantern.restopulse.data.services.CallLogDataSource
import com.redlantern.restopulse.models.CallType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class CallRepository @Inject constructor(
    private val dao: CallHistoryDao,
    private val callLog: CallLogDataSource,
    private val customers: CustomerRepository
) {
    data class FrequentContactsResult(
        val uniqueNumbersInLog: Int,
        val frequentNumbers: Int,
        val renamedContacts: Int,
        val formattedExistingContacts: Int
    )

    fun observeRecent(): Flow<List<CallHistoryEntity>> = dao.observeRecent()
    fun observeForCustomer(customerId: Long): Flow<List<CallHistoryEntity>> = dao.observeForCustomer(customerId)
    fun observeCallsSince(start: Long): Flow<Int> = dao.observeCallsSince(start)

    suspend fun importLatest(): CallHistoryEntity? {
        val latest = callLog.latestCall() ?: return null
        val customer = customers.findByNormalized(latest.normalizedNumber)
        val enriched = latest.copy(customerId = customer?.id, savedToCustomer = customer != null)
        val id = dao.insert(enriched)
        if (id == -1L) return null
        if (customer != null && id != -1L) {
            val (incoming, outgoing, missed) = dao.statFor(enriched.callType)
            dao.updateCustomerStats(customer.id, enriched.callDate, enriched.durationSeconds, incoming, outgoing, missed)
        }
        return enriched
    }

    suspend fun importRecent(limit: Int = 300) {
        callLog.read(limit).forEach { call ->
            val customer = customers.findByNormalized(call.normalizedNumber)
            val enriched = call.copy(customerId = customer?.id, savedToCustomer = customer != null)
            val id = dao.insert(enriched)
            if (customer != null && id != -1L) {
                val (incoming, outgoing, missed) = dao.statFor(enriched.callType)
                dao.updateCustomerStats(customer.id, enriched.callDate, enriched.durationSeconds, incoming, outgoing, missed)
            }
        }
    }

    suspend fun importAllAndCreateContacts(limit: Int? = INITIAL_IMPORT_CALL_LIMIT) {
        val allCalls = callLog.read(limit = limit)
        var generatedNumber = customers.nextAvailableGeneratedNumber()
        val existingAndroidNumbers = customers.existingAndroidNumbers()
        val customerByNumber = mutableMapOf<String, com.redlantern.restopulse.data.database.entities.CustomerEntity?>()
        var insertedCalls = 0
        var linkedCustomers = 0

        allCalls.forEach { call ->
            val customer = if (customerByNumber.containsKey(call.normalizedNumber)) {
                customerByNumber[call.normalizedNumber]
            } else {
                val existing = customers.findByNormalized(call.normalizedNumber)
                val imported = existing ?: customers.importUnknownFromCall(
                    call = call,
                    generatedNumber = generatedNumber,
                    existingAndroidNumbers = existingAndroidNumbers
                )?.also {
                    generatedNumber++
                }
                customerByNumber[call.normalizedNumber] = imported
                imported
            }
            val enriched = call.copy(customerId = customer?.id, savedToCustomer = customer != null)
            val id = dao.insert(enriched)
            if (customer != null && id != -1L) {
                insertedCalls++
                linkedCustomers++
                val (incoming, outgoing, missed) = dao.statFor(enriched.callType)
                dao.updateCustomerStats(customer.id, enriched.callDate, enriched.durationSeconds, incoming, outgoing, missed)
            } else if (id != -1L) {
                insertedCalls++
            }
        }
        Log.i(TAG, "Initial import read=${allCalls.size}, callsInserted=$insertedCalls, linkedCustomers=$linkedCustomers, uniqueNumbers=${customerByNumber.size}")
        runCatching { customers.backfillGeneratedContactNames() }
    }

    /**
     * Finds repeat callers across the complete device call log. Each normalized
     * number is handled once, even when Android stores it in several formats.
     */
    suspend fun organizeFrequentExistingContacts(minimumGapMillis: Long = THREE_HOURS_MILLIS): FrequentContactsResult {
        val formattedExistingContacts = customers.backfillGeneratedContactNames()
        val grouped = callLog.read(limit = null)
            .filter { it.callType in inboundCallTypes }
            .groupBy { it.normalizedNumber }
        val frequent = grouped.values
            .filter { calls -> calls.hasCallsSeparatedBy(minimumGapMillis) }
            .sortedWith(compareByDescending<List<CallHistoryEntity>> { it.size }
                .thenByDescending { calls -> calls.maxOf { it.callDate } })

        val assignedNames = customers.renameFrequentAndroidContacts(frequent.map { it.first().phoneNumber })
        assignedNames.forEach { (normalized, name) ->
            grouped[normalized]?.let { calls ->
                val latest = calls.maxBy { it.callDate }
                customers.indexFrequentContact(latest, name, calls.size)
            }
        }
        return FrequentContactsResult(grouped.size, frequent.size, assignedNames.size, formattedExistingContacts)
    }

    /** Promotes one newly active saved contact after a call, without retaining the full log. */
    suspend fun promoteIfNowFrequent(normalizedNumber: String): Boolean {
        val calls = callLog.readForNormalized(normalizedNumber)
            .filter { it.callType in inboundCallTypes }
        if (!calls.hasCallsSeparatedBy(THREE_HOURS_MILLIS)) return false

        val latest = calls.maxBy { it.callDate }
        val assignedName = customers.renameFrequentAndroidContacts(listOf(latest.phoneNumber))[normalizedNumber]
            ?: return false
        customers.indexFrequentContact(latest, assignedName, calls.size)
        return true
    }

    private fun List<CallHistoryEntity>.hasCallsSeparatedBy(minimumGapMillis: Long): Boolean {
        if (size < 2) return false
        val earliest = minOf { it.callDate }
        val latest = maxOf { it.callDate }
        return latest - earliest >= minimumGapMillis
    }

    private companion object {
        const val TAG = "RestoPulseImport"
        const val INITIAL_IMPORT_CALL_LIMIT = 600
        const val THREE_HOURS_MILLIS = 3L * 60 * 60 * 1000
        val inboundCallTypes = setOf(
            CallType.INCOMING,
            CallType.MISSED,
            CallType.REJECTED,
            CallType.BLOCKED
        )
    }
}
