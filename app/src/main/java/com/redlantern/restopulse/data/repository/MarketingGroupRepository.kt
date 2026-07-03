package com.redlantern.restopulse.data.repository

import com.redlantern.restopulse.data.database.dao.MarketingGroupDao
import com.redlantern.restopulse.data.database.dao.CustomerDao
import com.redlantern.restopulse.data.database.entities.CustomerEntity
import com.redlantern.restopulse.data.database.entities.MarketingGroupEntity
import com.redlantern.restopulse.data.preferences.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class MarketingGroupRepository @Inject constructor(
    private val dao: MarketingGroupDao,
    private val customerDao: CustomerDao,
    private val settings: SettingsRepository
) {
    private val batchMutex = Mutex()

    fun observeGroups(): Flow<List<MarketingGroupEntity>> = dao.observeGroups()
    fun observeMembers(groupId: Long): Flow<List<CustomerEntity>> = customerDao.observeCustomersInGroup(groupId)

    /**
     * Assigns every unassigned customer to exactly one persistent broadcast batch.
     * Customer.groupId is a single database column, so a customer cannot overlap batches.
     */
    suspend fun prepareBroadcastBatches(): Int = batchMutex.withLock {
        val maxSize = settings.settings.first().maxGroupSize.coerceAtLeast(1)
        val unassigned = customerDao.unassignedCustomers()
        if (unassigned.isEmpty()) return@withLock 0

        val groups = dao.all().toMutableList()
        val sizes = groups.associate { it.id to dao.size(it.id) }.toMutableMap()
        var assigned = 0

        unassigned.forEach { customer ->
            var target = groups.firstOrNull { (sizes[it.id] ?: 0) < it.maxSize }
            if (target == null) {
                val next = (groups.mapNotNull { it.name.substringAfterLast(' ').toIntOrNull() }.maxOrNull() ?: 0) + 1
                val now = System.currentTimeMillis()
                val newGroup = MarketingGroupEntity(
                    name = "Broadcast $next",
                    maxSize = maxSize,
                    createdAt = now,
                    updatedAt = now
                )
                val id = dao.insert(newGroup)
                target = newGroup.copy(id = id)
                groups += target
                sizes[id] = 0
            }
            customerDao.updateGroup(customer.id, target.id)
            sizes[target.id] = (sizes[target.id] ?: 0) + 1
            assigned++
        }
        assigned
    }

    suspend fun ensureGroupForNewCustomer(): Long {
        val maxSize = settings.settings.first().maxGroupSize
        val latest = dao.latest()
        if (latest == null || dao.size(latest.id) >= maxSize) {
            val next = ((latest?.name?.substringAfterLast(' ')?.toIntOrNull() ?: 0) + 1)
            val now = System.currentTimeMillis()
            return dao.insert(MarketingGroupEntity(name = "Broadcast $next", maxSize = maxSize, createdAt = now, updatedAt = now))
        }
        return latest.id
    }

    suspend fun rename(group: MarketingGroupEntity, name: String) = dao.update(group.copy(name = name, updatedAt = System.currentTimeMillis()))
    suspend fun merge(sourceGroup: Long, targetGroup: Long) {
        dao.merge(sourceGroup, targetGroup)
        dao.delete(sourceGroup)
    }
}
