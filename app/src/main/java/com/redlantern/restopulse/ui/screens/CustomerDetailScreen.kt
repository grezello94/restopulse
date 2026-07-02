package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import com.redlantern.restopulse.utils.readable
import com.redlantern.restopulse.viewmodels.CustomerDetailViewModel

@Composable
fun CustomerDetailScreen(padding: PaddingValues, onBack: () -> Unit, vm: CustomerDetailViewModel = hiltViewModel()) {
    val customer by vm.customer.collectAsState()
    val history by vm.history.collectAsState()
    val current = customer ?: return
    var draft by remember(current.id, current.dateAdded) { mutableStateOf(current) }
    LazyColumn(
        contentPadding = PaddingValues(top = padding.calculateTopPadding() + 12.dp, bottom = padding.calculateBottomPadding() + 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Text("Customer Profile")
            }
        }
        item { OutlinedTextField(draft.name, { draft = draft.copy(name = it) }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(draft.phoneNumber, {}, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), readOnly = true) }
        item { OutlinedTextField(draft.customerTag, { draft = draft.copy(customerTag = it) }, label = { Text("Tag") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(draft.notes, { draft = draft.copy(notes = it) }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3) }
        item { OutlinedTextField(draft.address, { draft = draft.copy(address = it) }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(draft.location, { draft = draft.copy(location = it) }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row { Checkbox(draft.favorite, { draft = draft.copy(favorite = it) }); Text("Favorite") }
                Row { Checkbox(draft.vip, { draft = draft.copy(vip = it) }); Text("VIP") }
                Row { Checkbox(draft.whatsappAvailable, { draft = draft.copy(whatsappAvailable = it) }); Text("WhatsApp") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { vm.save(draft) }) { Text("Save") }
                Button(onClick = { vm.addToAndroidContacts(draft) }) { Text("Add Contact") }
            }
        }
        item { Text("Timeline") }
        items(history, key = { it.id }) {
            Card {
                ListItem(
                    headlineContent = { Text(it.callType.name.lowercase()) },
                    supportingContent = { Text(it.callDate.readable()) },
                    trailingContent = { Text("${it.durationSeconds}s") }
                )
            }
        }
    }
}
