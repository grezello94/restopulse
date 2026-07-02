package com.redlantern.restopulse.data.repository

import com.redlantern.restopulse.data.database.dao.CallHistoryDao
import com.redlantern.restopulse.data.database.entities.CallHistoryEntity
import com.redlantern.restopulse.data.services.CallLogDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class CallRepository @Inject constructor(
    private val dao: CallHistoryDao,
    private val callLog: CallLogDataSource,
    private val customers: CustomerRepository
) {
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

    suspend fun importAllAndCreateContacts() {
        val allCalls = callLog.read(limit = null)
        var generatedNumber = customers.nextAvailableGeneratedNumber()
        val customerByNumber = mutableMapOf<String, com.redlantern.restopulse.data.database.entities.CustomerEntity?>()

        allCalls.forEach { call ->
            val customer = if (customerByNumber.containsKey(call.normalizedNumber)) {
                customerByNumber[call.normalizedNumber]
            } else {
                val existing = customers.findByNormalized(call.normalizedNumber)
                val imported = existing ?: customers.importUnknownFromCall(call, generatedNumber)?.also {
                    generatedNumber = customers.nextAvailableGeneratedNumber()
                }
                customerByNumber[call.normalizedNumber] = imported
                imported
            }
            val enriched = call.copy(customerId = customer?.id, savedToCustomer = customer != null)
            val id = dao.insert(enriched)
            if (customer != null && id != -1L) {
                val (incoming, outgoing, missed) = dao.statFor(enriched.callType)
                dao.updateCustomerStats(customer.id, enriched.callDate, enriched.durationSeconds, incoming, outgoing, missed)
            }
        }
    }
}
