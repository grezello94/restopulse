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
        val id = customers.addCustomerFromCall(call)
        if (id > 0) customers.setGroup(id, groups.ensureGroupForNewCustomer())
        return id
    }
}
