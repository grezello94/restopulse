package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redlantern.restopulse.ui.components.ContentCard
import com.redlantern.restopulse.ui.components.EmptyState
import com.redlantern.restopulse.ui.components.SectionTitle
import com.redlantern.restopulse.ui.components.StatCard
import com.redlantern.restopulse.ui.components.ScreenHeader
import com.redlantern.restopulse.ui.components.StatusPill
import com.redlantern.restopulse.ui.components.StatusTone
import com.redlantern.restopulse.ui.components.screenPadding
import com.redlantern.restopulse.utils.readable
import com.redlantern.restopulse.viewmodels.DashboardScanState
import com.redlantern.restopulse.viewmodels.DashboardViewModel
import com.redlantern.restopulse.viewmodels.QuickWhatsAppViewModel

@Composable
fun DashboardScreen(
    padding: PaddingValues,
    onCustomer: () -> Unit,
    vm: DashboardViewModel = hiltViewModel(),
    quickWhatsAppVm: QuickWhatsAppViewModel = hiltViewModel()
) {
    val stats by vm.stats.collectAsState()
    val recent by vm.recentCalls.collectAsState()
    val scanState by vm.scanState.collectAsState()
    val quickWhatsApp by quickWhatsAppVm.state.collectAsState()
    LazyColumn(
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenHeader("RestoPulse", "Live customer activity for Red Lantern") }
        item {
            ContentCard {
                androidx.compose.foundation.layout.Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Quick WhatsApp", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = quickWhatsApp.input,
                        onValueChange = quickWhatsAppVm::setInput,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text("Mobile number") },
                        placeholder = { Text("Type customer number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        )
                    )
                    Button(
                        onClick = quickWhatsAppVm::search,
                        enabled = !quickWhatsApp.checking,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (quickWhatsApp.checking) "Checking..." else "Search WhatsApp")
                    }
                    Text(quickWhatsApp.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (quickWhatsApp.hasSearch) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = quickWhatsAppVm::openPersonal,
                                enabled = quickWhatsApp.personalAvailable,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("WhatsApp")
                            }
                            OutlinedButton(
                                onClick = quickWhatsAppVm::openBusiness,
                                enabled = quickWhatsApp.businessAvailable,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Business")
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Customers", stats.totalCustomers.toString(), Modifier.weight(1f), Icons.Default.People)
                StatCard("New Today", stats.newCustomersToday.toString(), Modifier.weight(1f), Icons.Default.Today)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Calls Today", stats.callsToday.toString(), Modifier.weight(1f), Icons.Default.Call)
                StatCard("WhatsApp", stats.whatsappCustomers.toString(), Modifier.weight(1f), Icons.Default.Message)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Frequent", stats.frequentCustomers.toString(), Modifier.weight(1f), Icons.Default.VerifiedUser)
                StatCard("Non WhatsApp", stats.nonWhatsappCustomers.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Duplicates Safe", stats.duplicatesPrevented.toString(), Modifier.weight(1f), Icons.Default.VerifiedUser)
                StatCard("Groups", stats.marketingGroups.toString(), Modifier.weight(1f), Icons.Default.Groups)
            }
        }
        item {
            ContentCard {
                ListItem(
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    headlineContent = { Text("Export status", style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text(stats.exportStatus) },
                    trailingContent = { StatusPill("Ready", tone = StatusTone.Positive) }
                )
            }
        }
        item { SectionTitle("Recent Calls", "Customers", onCustomer) }
        items(recent.take(8), key = { it.id }, contentType = { "recent-call-row" }) {
            ContentCard {
                ListItem(
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    headlineContent = { Text(it.phoneNumber, style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text("${it.callType.name.lowercase()} • ${it.callDate.readable()}") },
                    trailingContent = { StatusPill("${it.durationSeconds}s") }
                )
            }
        }
        if (recent.isEmpty()) {
            item { EmptyState("No calls yet", "Recent calls will appear here after the first sync.") }
        }
        item { SectionTitle("Today's Activity") }
        item {
            ContentCard {
                val scanMessage = when (val state = scanState) {
                    DashboardScanState.Idle -> "Preparing the first scan."
                    DashboardScanState.Scanning -> "Scanning call history and contacts in the background."
                    DashboardScanState.Complete -> "Discovery, duplicate prevention, frequent-customer naming, and call history sync are active."
                    is DashboardScanState.Failed -> "Scan failed: ${state.message}"
                }
                val scanTone = when (scanState) {
                    DashboardScanState.Complete -> StatusTone.Positive
                    is DashboardScanState.Failed -> StatusTone.Warning
                    else -> StatusTone.Neutral
                }
                ListItem(
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    headlineContent = { Text("Automation", style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text(scanMessage) },
                    trailingContent = {
                        if (scanState is DashboardScanState.Failed) {
                            OutlinedButton(onClick = vm::startInitialScan) { Text("Retry") }
                        } else {
                            StatusPill(
                                if (scanState == DashboardScanState.Scanning) "Scanning" else "Ready",
                                tone = scanTone
                            )
                        }
                    }
                )
            }
        }
    }
}
