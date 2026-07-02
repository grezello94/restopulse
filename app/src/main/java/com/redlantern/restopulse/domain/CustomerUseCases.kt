package com.redlantern.restopulse.domain

import com.redlantern.restopulse.data.database.entities.CallHistoryEntity
import com.redlantern.restopulse.data.repository.CustomerRepository
import com.redlantern.restopulse.data.repository.MarketingGroupRepository
import javax.inject.Inject

class CustomerUseCases @Inject constructor(
    private val customers: CustomerRepository,
    private val groups: MarketingGroupRepository
) {
    suspend fun addDiscoveredCustomer(call: CallHistoryEntity): Long {
        val customer = customers.importUnknownFromCall(call, customers.nextAvailableGeneratedNumber())
            ?: return -1
        val id = customer.id
        if (id > 0) customers.setGroup(id, groups.ensureGroupForNewCustomer())
        return id
    }
}
