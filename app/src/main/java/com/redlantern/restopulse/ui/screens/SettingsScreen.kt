package com.redlantern.restopulse.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redlantern.restopulse.models.ExportFormat
import com.redlantern.restopulse.viewmodels.ExportViewModel
import com.redlantern.restopulse.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    padding: PaddingValues,
    vm: SettingsViewModel = hiltViewModel(),
    exportVm: ExportViewModel = hiltViewModel()
) {
    val settings by vm.settings.collectAsState()
    val namePrefix by exportVm.namePrefix.collectAsState()
    val matchingCount by exportVm.matchingCount.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        exportVm.shareIntent.collect { context.startActivity(Intent.createChooser(it, "Share with")) }
    }
    LazyColumn(
        contentPadding = PaddingValues(top = padding.calculateTopPadding() + 16.dp, bottom = padding.calculateBottomPadding() + 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Settings") }
        item {
            ListItem(
                headlineContent = { Text("Dynamic Material You") },
                supportingContent = { Text("Use system colors where supported") },
                trailingContent = { Switch(settings.dynamicColor, vm::setDynamicColor) }
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Marketing group size") },
                supportingContent = { Slider(settings.maxGroupSize.toFloat(), { vm.setMaxGroupSize(it.toInt()) }, valueRange = 25f..1000f) },
                trailingContent = { Text(settings.maxGroupSize.toString()) }
            )
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Export contacts by name") },
                    supportingContent = { Text("Enter the beginning of the contact name, such as RL or RL Customer. Matching is not case-sensitive.") }
                )
                OutlinedTextField(
                    value = namePrefix,
                    onValueChange = exportVm::setNamePrefix,
                    label = { Text("Contact name starts with") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = exportVm::exportMatchingExcel,
                    enabled = matchingCount > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save $matchingCount matching contacts to Excel")
                }
            }
        }
        item { Button(onClick = { exportVm.export(ExportFormat.CSV) }) { Text("Export CSV") } }
        item { Button(onClick = { exportVm.export(ExportFormat.EXCEL) }) { Text("Export Excel") } }
        item { Button(onClick = { exportVm.export(ExportFormat.VCF) }) { Text("Export VCF") } }
        item { Button(onClick = { exportVm.export(ExportFormat.PDF) }) { Text("Export PDF Report") } }
        item { Button(onClick = exportVm::backup) { Text("Backup Database") } }
        item { ListItem(headlineContent = { Text("Permission Manager") }, supportingContent = { Text("Review Contacts, Call Log, Phone State, and Notifications access in Android Settings") }) }
    }
}
