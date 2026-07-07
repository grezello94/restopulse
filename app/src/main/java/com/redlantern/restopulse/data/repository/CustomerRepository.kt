package com.redlantern.restopulse.data.repository

import com.redlantern.restopulse.data.database.dao.CallHistoryDao
import com.redlantern.restopulse.data.database.dao.CustomerDao
import com.redlantern.restopulse.data.database.entities.CallHistoryEntity
import com.redlantern.restopulse.data.database.entities.CustomerEntity
import com.redlantern.restopulse.data.database.entities.IgnoredNumberEntity
import com.redlantern.restopulse.data.services.ContactsDataSource
import com.redlantern.restopulse.data.services.WhatsAppChecker
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao,
    private val callHistoryDao: CallHistoryDao,
    private val contacts: ContactsDataSource,
    private val whatsAppChecker: WhatsAppChecker
) {
    fun observeCustomers(): Flow<List<CustomerEntity>> = customerDao.observeCustomers()
    fun search(query: String): Flow<List<CustomerEntity>> = if (query.isBlank()) observeCustomers() else customerDao.search(query.trim())
    fun observeCustomer(id: Long): Flow<CustomerEntity?> = customerDao.observeCustomer(id)
    fun observeCount(): Flow<Int> = customerDao.observeCustomerCount()
    fun observeNewSince(start: Long): Flow<Int> = customerDao.observeNewSince(start)
    fun observeWhatsappCount(): Flow<Int> = customerDao.observeWhatsappCount()
    fun observeNonWhatsappCount(): Flow<Int> = customerDao.observeNonWhatsappCount()
    fun observeFrequentCount(): Flow<Int> = customerDao.observeFrequentCount()
    fun observeDuplicateCount(): Flow<Int> = customerDao.observeDuplicateCount()

    suspend fun addCustomerFromCall(call: CallHistoryEntity): Long {
        val existing = customerDao.findByNormalized(call.normalizedNumber)
        if (existing != null || contacts.contactExists(call.phoneNumber)) {
            customerDao.insertDuplicate(com.redlantern.restopulse.data.database.entities.DuplicateEventEntity(normalizedNumber = call.normalizedNumber, detectedAt = System.currentTimeMillis(), reason = "Existing customer or Android contact"))
            return -1
        }
        val customer = CustomerEntity(
            name = call.callerName,
            phoneNumber = call.phoneNumber,
            normalizedNumber = call.normalizedNumber,
            dateAdded = System.currentTimeMillis(),
            lastCallDate = call.callDate,
            lastCallDuration = call.durationSeconds,
            whatsappAvailable = whatsAppChecker.canOpenChat(call.normalizedNumber)
        )
        return customerDao.insertOrDuplicate(customer)
    }

    /** Indexes a saved contact, or creates a contact for a previously unknown caller. */
    suspend fun importUnknownFromCall(
        call: CallHistoryEntity,
        generatedNumber: Int,
        existingAndroidNumbers: MutableSet<String>? = null
    ): CustomerEntity? {
        if (customerDao.findByNormalized(call.normalizedNumber) != null || isIgnored(call.normalizedNumber)) return null

        val alreadySaved = existingAndroidNumbers?.contains(call.normalizedNumber) ?: contacts.contactExists(call.phoneNumber)
        val name = if (alreadySaved) {
            call.callerName.ifBlank { call.phoneNumber }
        } else {
            "RL Customer $generatedNumber [${call.normalizedNumber.takeLast(4)}]"
        }
        val customer = CustomerEntity(
            name = name,
            phoneNumber = call.phoneNumber,
            normalizedNumber = call.normalizedNumber,
            dateAdded = System.currentTimeMillis(),
            lastCallDate = call.callDate,
            lastCallDuration = call.durationSeconds,
            whatsappAvailable = runCatching { whatsAppChecker.canOpenChat(call.normalizedNumber) }.getOrDefault(false)
        )
        val id = customerDao.insertOrDuplicate(customer)
        if (id == -1L) return null

        // Save RestoPulse's own row first so Home updates quickly. Android
        // Contacts writes are slower and device-dependent, so they must not
        // block the dashboard counters.
        if (!alreadySaved) {
            if (existingAndroidNumbers == null) {
                runCatching { contacts.addContactIfAbsent(name, call.phoneNumber) }
            } else {
                runCatching {
                    contacts.addContact(name, call.phoneNumber)
                    existingAndroidNumbers += call.normalizedNumber
                }
            }
        }
        return customer.copy(id = id)
    }

    fun nextAvailableGeneratedNumber(): Int = contacts.nextAvailableCustomerNumber()
    fun existingAndroidNumbers(): MutableSet<String> = contacts.existingNormalizedNumbers().toMutableSet()
    fun isAndroidContact(number: String): Boolean = contacts.contactExists(number)
    fun renameAndroidContact(number: String, name: String): Boolean = contacts.renameExistingContact(number, name)
    fun frequentAndroidContactName(number: String): String? = contacts.frequentCustomerName(number)
    fun nextAvailableFrequentNumber(): Int = contacts.nextAvailableFrequentCustomerNumber()
    fun renameFrequentAndroidContacts(numbers: List<String>): Map<String, String> =
        contacts.renameFrequentContacts(numbers)

    suspend fun backfillGeneratedContactNames(): Int {
        val renamed = contacts.backfillGeneratedContactNames()
        renamed.forEach { (normalized, name) -> customerDao.updateNameByNormalized(normalized, name) }
        return renamed.size
    }

    suspend fun save(customer: CustomerEntity) = customerDao.update(customer)
    suspend fun ignore(number: String, normalized: String) = customerDao.insertIgnored(IgnoredNumberEntity(normalized, number, System.currentTimeMillis()))
    suspend fun isIgnored(normalized: String): Boolean = customerDao.findIgnored(normalized) != null
    suspend fun findByNormalized(normalized: String): CustomerEntity? = customerDao.findByNormalized(normalized)
    suspend fun indexFrequentContact(call: CallHistoryEntity, name: String, totalCalls: Int) {
        val existing = customerDao.findByNormalized(call.normalizedNumber)
        if (existing != null) {
            customerDao.update(existing.copy(name = name, totalCalls = maxOf(existing.totalCalls, totalCalls)))
            return
        }
        customerDao.insertOrDuplicate(
            CustomerEntity(
                name = name,
                phoneNumber = call.phoneNumber,
                normalizedNumber = call.normalizedNumber,
                dateAdded = System.currentTimeMillis(),
                lastCallDate = call.callDate,
                lastCallDuration = call.durationSeconds,
                whatsappAvailable = whatsAppChecker.canOpenChat(call.normalizedNumber),
                totalCalls = totalCalls
            )
        )
    }
    suspend fun addToAndroidContacts(customer: CustomerEntity) =
        contacts.addContactIfAbsent(customer.name, customer.phoneNumber)
    suspend fun setGroup(customerId: Long, groupId: Long?) = customerDao.updateGroup(customerId, groupId)
}
