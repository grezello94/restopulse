package com.redlantern.restopulse.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redlantern.restopulse.models.ExportFormat
import com.redlantern.restopulse.viewmodels.ExportViewModel
import com.redlantern.restopulse.viewmodels.SettingsViewModel
import com.redlantern.restopulse.ui.components.ContentCard
import com.redlantern.restopulse.ui.components.ScreenHeader
import com.redlantern.restopulse.ui.components.SectionTitle
import com.redlantern.restopulse.ui.components.StatusPill
import com.redlantern.restopulse.ui.components.StatusTone
import com.redlantern.restopulse.ui.components.screenPadding
import androidx.compose.material3.MaterialTheme

@Composable
fun SettingsScreen(
    padding: PaddingValues,
    vm: SettingsViewModel = hiltViewModel(),
    exportVm: ExportViewModel = hiltViewModel()
) {
    val settings by vm.settings.collectAsState()
    val frequentStatus by vm.frequentContactsStatus.collectAsState()
    val organizing by vm.organizingFrequentContacts.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        exportVm.shareIntent.collect { context.startActivity(Intent.createChooser(it, "Share with")) }
    }
    LazyColumn(
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenHeader("Settings", "Control contact naming, exports, and device behavior") }
        item { SectionTitle("Contact Organizer") }
        item {
            ContentCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Frequent WhatsApp customers", style = MaterialTheme.typography.titleMedium)
                            Text("Rename existing contacts without creating duplicates.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill("Safe", tone = StatusTone.Positive)
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        ListItem(
                            colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                            headlineContent = { Text("Preview") },
                            supportingContent = { Text("RL Frq Customer 35 [7584]") },
                            trailingContent = { StatusPill("last 4 + number") }
                        )
                    }
                    Text(
                        frequentStatus ?: "Calls must be at least three hours apart. Duplicate phone numbers are combined before renaming.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = vm::organizeFrequentContacts, enabled = !organizing, modifier = Modifier.fillMaxWidth()) {
                        Text(if (organizing) "Checking contacts..." else "Organize frequent contacts")
                    }
                }
            }
        }
        item { SectionTitle("Preferences") }
        item {
            ContentCard { ListItem(
                colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                headlineContent = { Text("Dynamic Material You") },
                supportingContent = { Text("Use system colors where supported") },
                trailingContent = { Switch(settings.dynamicColor, vm::setDynamicColor) }
            ) }
        }
        item {
            ContentCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Marketing group size", style = MaterialTheme.typography.titleMedium)
                        StatusPill(settings.maxGroupSize.toString())
                    }
                    Slider(settings.maxGroupSize.toFloat(), { vm.setMaxGroupSize(it.toInt()) }, valueRange = 25f..1000f)
                }
            }
        }
        item { SectionTitle("Exports") }
        item {
            ContentCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { exportVm.export(ExportFormat.CSV) }, modifier = Modifier.weight(1f)) { Text("CSV") }
                        OutlinedButton(onClick = { exportVm.export(ExportFormat.EXCEL) }, modifier = Modifier.weight(1f)) { Text("Excel") }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { exportVm.export(ExportFormat.VCF) }, modifier = Modifier.weight(1f)) { Text("VCF") }
                        OutlinedButton(onClick = { exportVm.export(ExportFormat.PDF) }, modifier = Modifier.weight(1f)) { Text("PDF") }
                    }
                    Button(onClick = exportVm::backup, modifier = Modifier.fillMaxWidth()) { Text("Backup database") }
                }
            }
        }
        item {
            ContentCard { ListItem(
                colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                headlineContent = { Text("Permission Manager") },
                supportingContent = { Text("Review Contacts, Call Log, Phone State, and Notifications access in Android Settings") }
            ) }
        }
    }
}
