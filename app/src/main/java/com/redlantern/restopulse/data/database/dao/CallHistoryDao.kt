package com.redlantern.restopulse.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.redlantern.restopulse.data.database.entities.CallHistoryEntity
import com.redlantern.restopulse.models.CallType
import kotlinx.coroutines.flow.Flow

@Dao
interface CallHistoryDao {
    @Query("SELECT * FROM call_history ORDER BY callDate DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<CallHistoryEntity>>

    @Query("SELECT * FROM call_history WHERE customerId = :customerId ORDER BY callDate DESC")
    fun observeForCustomer(customerId: Long): Flow<List<CallHistoryEntity>>

    @Query("SELECT COUNT(*) FROM call_history WHERE callDate >= :start")
    fun observeCallsSince(start: Long): Flow<Int>

    @Query("SELECT * FROM call_history ORDER BY callDate DESC LIMIT 1")
    suspend fun latest(): CallHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(history: CallHistoryEntity): Long

    @Query("""
        UPDATE customers SET
        lastCallDate = CASE WHEN :callDate > lastCallDate THEN :callDate ELSE lastCallDate END,
        lastCallDuration = CASE WHEN :callDate >= lastCallDate THEN :duration ELSE lastCallDuration END,
        incomingCount = incomingCount + :incoming,
        outgoingCount = outgoingCount + :outgoing,
        missedCount = missedCount + :missed,
        totalCalls = totalCalls + 1
        WHERE id = :customerId
    """)
    suspend fun updateCustomerStats(customerId: Long, callDate: Long, duration: Long, incoming: Int, outgoing: Int, missed: Int)

    suspend fun statFor(type: CallType): Triple<Int, Int, Int> = when (type) {
        CallType.INCOMING -> Triple(1, 0, 0)
        CallType.OUTGOING -> Triple(0, 1, 0)
        CallType.MISSED, CallType.REJECTED, CallType.BLOCKED -> Triple(0, 0, 1)
        CallType.UNKNOWN -> Triple(0, 0, 0)
    }
}
