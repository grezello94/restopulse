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

    /** Creates both the CRM customer and its corresponding Android contact. */
    suspend fun importUnknownFromCall(call: CallHistoryEntity, generatedNumber: Int): CustomerEntity? {
        if (customerDao.findByNormalized(call.normalizedNumber) != null ||
            contacts.contactExists(call.phoneNumber) ||
            isIgnored(call.normalizedNumber)
        ) return null

        val name = "RL Customer $generatedNumber"
        contacts.addContact(name, call.phoneNumber)
        val customer = CustomerEntity(
            name = name,
            phoneNumber = call.phoneNumber,
            normalizedNumber = call.normalizedNumber,
            dateAdded = System.currentTimeMillis(),
            lastCallDate = call.callDate,
            lastCallDuration = call.durationSeconds,
            whatsappAvailable = whatsAppChecker.canOpenChat(call.normalizedNumber)
        )
        val id = customerDao.insertOrDuplicate(customer)
        return if (id == -1L) null else customer.copy(id = id)
    }

    fun nextAvailableGeneratedNumber(): Int = contacts.nextAvailableCustomerNumber()
    fun isAndroidContact(number: String): Boolean = contacts.contactExists(number)

    suspend fun save(customer: CustomerEntity) = customerDao.update(customer)
    suspend fun ignore(number: String, normalized: String) = customerDao.insertIgnored(IgnoredNumberEntity(normalized, number, System.currentTimeMillis()))
    suspend fun isIgnored(normalized: String): Boolean = customerDao.findIgnored(normalized) != null
    suspend fun findByNormalized(normalized: String): CustomerEntity? = customerDao.findByNormalized(normalized)
    suspend fun addToAndroidContacts(customer: CustomerEntity) = contacts.addContact(customer.name, customer.phoneNumber)
    suspend fun setGroup(customerId: Long, groupId: Long?) = customerDao.updateGroup(customerId, groupId)
}
