package com.redlantern.restopulse.domain

data class DashboardStats(
    val totalCustomers: Int = 0,
    val newCustomersToday: Int = 0,
    val callsToday: Int = 0,
    val whatsappCustomers: Int = 0,
    val nonWhatsappCustomers: Int = 0,
    val duplicatesPrevented: Int = 0,
    val marketingGroups: Int = 0,
    val exportStatus: String = "Ready"
)
