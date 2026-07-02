package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redlantern.restopulse.ui.components.SectionTitle
import com.redlantern.restopulse.ui.components.StatCard
import com.redlantern.restopulse.utils.readable
import com.redlantern.restopulse.viewmodels.DashboardViewModel

@Composable
fun DashboardScreen(padding: PaddingValues, onCustomer: () -> Unit, vm: DashboardViewModel = hiltViewModel()) {
    val stats by vm.stats.collectAsState()
    val recent by vm.recentCalls.collectAsState()
    LazyColumn(
        contentPadding = PaddingValues(top = padding.calculateTopPadding() + 16.dp, bottom = padding.calculateBottomPadding() + 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Red Lantern CRM", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Total Customers", stats.totalCustomers.toString(), Modifier.weight(1f))
                StatCard("New Today", stats.newCustomersToday.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Calls Today", stats.callsToday.toString(), Modifier.weight(1f))
                StatCard("WhatsApp", stats.whatsappCustomers.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Non WhatsApp", stats.nonWhatsappCustomers.toString(), Modifier.weight(1f))
                StatCard("Duplicates Prevented", stats.duplicatesPrevented.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Marketing Groups", stats.marketingGroups.toString(), Modifier.weight(1f))
                StatCard("Export Status", stats.exportStatus, Modifier.weight(1f))
            }
        }
        item { SectionTitle("Recent Calls", "Customers", onCustomer) }
        items(recent.take(8), key = { it.id }) {
            Card { ListItem(headlineContent = { Text(it.phoneNumber) }, supportingContent = { Text("${it.callType.name.lowercase()} • ${it.callDate.readable()} • ${it.durationSeconds}s") }) }
        }
        item { SectionTitle("Today's Activity") }
        item { Text("Customer discovery, duplicate prevention, and call history sync are active.", Modifier.padding(horizontal = 4.dp)) }
    }
}
