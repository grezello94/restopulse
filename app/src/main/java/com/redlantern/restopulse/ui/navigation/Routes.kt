package com.redlantern.restopulse.ui.navigation

sealed class Route(val path: String) {
    data object Dashboard : Route("dashboard")
    data object Customers : Route("customers")
    data object Calls : Route("calls")
    data object Groups : Route("groups")
    data object BroadcastBatch : Route("groups/{groupId}") {
        fun create(groupId: Long) = "groups/$groupId"
    }
    data object Analytics : Route("analytics")
    data object Settings : Route("settings")
    data object CustomerDetail : Route("customer/{customerId}") {
        fun create(customerId: Long) = "customer/$customerId"
    }
}
