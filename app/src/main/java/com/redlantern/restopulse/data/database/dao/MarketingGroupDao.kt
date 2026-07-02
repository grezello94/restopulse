package com.redlantern.restopulse.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.redlantern.restopulse.data.database.entities.MarketingGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketingGroupDao {
    @Query("SELECT * FROM marketing_groups ORDER BY id")
    fun observeGroups(): Flow<List<MarketingGroupEntity>>

    @Query("SELECT COUNT(*) FROM customers WHERE groupId = :groupId")
    suspend fun size(groupId: Long): Int

    @Query("SELECT * FROM marketing_groups ORDER BY id DESC LIMIT 1")
    suspend fun latest(): MarketingGroupEntity?

    @Insert
    suspend fun insert(group: MarketingGroupEntity): Long

    @Update
    suspend fun update(group: MarketingGroupEntity)

    @Query("DELETE FROM marketing_groups WHERE id = :groupId")
    suspend fun delete(groupId: Long)

    @Query("UPDATE customers SET groupId = :targetGroup WHERE groupId = :sourceGroup")
    suspend fun merge(sourceGroup: Long, targetGroup: Long)
}
