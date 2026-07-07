package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redlantern.restopulse.ui.components.ContentCard
import com.redlantern.restopulse.ui.components.EmptyState
import com.redlantern.restopulse.ui.components.IconBubble
import com.redlantern.restopulse.ui.components.SectionTitle
import com.redlantern.restopulse.ui.components.StatusPill
import com.redlantern.restopulse.ui.components.StatusTone
import com.redlantern.restopulse.ui.components.screenPadding
import com.redlantern.restopulse.utils.readable
import com.redlantern.restopulse.viewmodels.CustomerDetailViewModel

@Composable
fun CustomerDetailScreen(padding: PaddingValues, onBack: () -> Unit, vm: CustomerDetailViewModel = hiltViewModel()) {
    val customer by vm.customer.collectAsState()
    val history by vm.history.collectAsState()
    val current = customer ?: return
    var draft by remember(current.id, current.dateAdded) { mutableStateOf(current) }
    LazyColumn(
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Text("Customer Profile", style = MaterialTheme.typography.titleLarge)
            }
        }
        item {
            ContentCard {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    IconBubble(Icons.Default.Person)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(draft.name.ifBlank { draft.phoneNumber }, style = MaterialTheme.typography.headlineSmall)
                        Text(draft.phoneNumber, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (draft.whatsappAvailable) StatusPill("WhatsApp", tone = StatusTone.Positive)
                            if (draft.vip) StatusPill("VIP", tone = StatusTone.Warning)
                            if (draft.favorite) StatusPill("Favorite")
                        }
                    }
                }
            }
        }
        item { SectionTitle("Profile") }
        item {
            ContentCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(draft.name, { draft = draft.copy(name = it) }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(draft.phoneNumber, {}, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                    OutlinedTextField(draft.customerTag, { draft = draft.copy(customerTag = it) }, label = { Text("Tag") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(draft.notes, { draft = draft.copy(notes = it) }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    OutlinedTextField(draft.address, { draft = draft.copy(address = it) }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(draft.location, { draft = draft.copy(location = it) }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item {
            ContentCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row { Checkbox(draft.favorite, { draft = draft.copy(favorite = it) }); Text("Favorite") }
                        Row { Checkbox(draft.vip, { draft = draft.copy(vip = it) }); Text("VIP") }
                        Row { Checkbox(draft.whatsappAvailable, { draft = draft.copy(whatsappAvailable = it) }); Text("WhatsApp") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { vm.save(draft) }, modifier = Modifier.weight(1f)) { Text("Save") }
                        Button(onClick = { vm.addToAndroidContacts(draft) }, modifier = Modifier.weight(1f)) { Text("Add Contact") }
                    }
                }
            }
        }
        item { SectionTitle("Timeline") }
        items(history, key = { it.id }, contentType = { "timeline-row" }) {
            ContentCard {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    headlineContent = { Text(it.callType.name.lowercase()) },
                    supportingContent = { Text(it.callDate.readable()) },
                    trailingContent = { StatusPill("${it.durationSeconds}s") }
                )
            }
        }
        if (history.isEmpty()) item { EmptyState("No timeline yet", "Calls for this customer will appear here.") }
    }
}
