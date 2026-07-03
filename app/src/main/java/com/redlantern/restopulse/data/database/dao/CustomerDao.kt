package com.redlantern.restopulse.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.redlantern.restopulse.data.database.entities.CustomerEntity
import com.redlantern.restopulse.data.database.entities.DuplicateEventEntity
import com.redlantern.restopulse.data.database.entities.IgnoredNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY lastCallDate DESC, dateAdded DESC")
    fun observeCustomers(): Flow<List<CustomerEntity>>

    @Query("""
        SELECT * FROM customers
        WHERE name LIKE '%' || :query || '%'
        OR phoneNumber LIKE '%' || :query || '%'
        OR normalizedNumber LIKE '%' || :query || '%'
        OR customerTag LIKE '%' || :query || '%'
        OR notes LIKE '%' || :query || '%'
        OR location LIKE '%' || :query || '%'
        ORDER BY lastCallDate DESC
    """)
    fun search(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun observeCustomer(id: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE groupId = :groupId ORDER BY dateAdded, id")
    fun observeCustomersInGroup(groupId: Long): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE groupId IS NULL ORDER BY dateAdded, id")
    suspend fun unassignedCustomers(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE normalizedNumber = :normalized LIMIT 1")
    suspend fun findByNormalized(normalized: String): CustomerEntity?

    @Query("SELECT COUNT(*) FROM customers")
    fun observeCustomerCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE dateAdded >= :start")
    fun observeNewSince(start: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE whatsappAvailable = 1")
    fun observeWhatsappCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE whatsappAvailable = 0")
    fun observeNonWhatsappCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM duplicate_events")
    fun observeDuplicateCount(): Flow<Int>

    @Query("SELECT * FROM ignored_numbers WHERE normalizedNumber = :normalized LIMIT 1")
    suspend fun findIgnored(normalized: String): IgnoredNumberEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIgnored(number: IgnoredNumberEntity)

    @Insert
    suspend fun insertDuplicate(event: DuplicateEventEntity)

    @Update
    suspend fun update(customer: CustomerEntity)

    @Query("UPDATE customers SET groupId = :groupId WHERE id = :customerId")
    suspend fun updateGroup(customerId: Long, groupId: Long?)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun delete(customerId: Long)

    @Transaction
    suspend fun insertOrDuplicate(customer: CustomerEntity): Long {
        val id = insert(customer)
        if (id == -1L) {
            insertDuplicate(DuplicateEventEntity(normalizedNumber = customer.normalizedNumber, detectedAt = System.currentTimeMillis(), reason = "Customer already exists"))
        }
        return id
    }
}
