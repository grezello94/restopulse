package com.redlantern.restopulse.data.repository

import com.redlantern.restopulse.data.database.dao.MarketingGroupDao
import com.redlantern.restopulse.data.database.entities.MarketingGroupEntity
import com.redlantern.restopulse.data.preferences.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Singleton
class MarketingGroupRepository @Inject constructor(
    private val dao: MarketingGroupDao,
    private val settings: SettingsRepository
) {
    fun observeGroups(): Flow<List<MarketingGroupEntity>> = dao.observeGroups()

    suspend fun ensureGroupForNewCustomer(): Long {
        val maxSize = settings.settings.first().maxGroupSize
        val latest = dao.latest()
        if (latest == null || dao.size(latest.id) >= maxSize) {
            val next = ((latest?.name?.substringAfterLast(' ')?.toIntOrNull() ?: 0) + 1)
            val now = System.currentTimeMillis()
            return dao.insert(MarketingGroupEntity(name = "Red Lantern Customers $next", maxSize = maxSize, createdAt = now, updatedAt = now))
        }
        return latest.id
    }

    suspend fun rename(group: MarketingGroupEntity, name: String) = dao.update(group.copy(name = name, updatedAt = System.currentTimeMillis()))
    suspend fun merge(sourceGroup: Long, targetGroup: Long) {
        dao.merge(sourceGroup, targetGroup)
        dao.delete(sourceGroup)
    }
}
