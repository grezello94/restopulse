package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redlantern.restopulse.viewmodels.BroadcastBatchViewModel

@Composable
fun BroadcastBatchScreen(
    padding: PaddingValues,
    onBack: () -> Unit,
    vm: BroadcastBatchViewModel = hiltViewModel()
) {
    val members by vm.members.collectAsState()
    val clipboard = LocalClipboardManager.current

    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding() + 12.dp,
            bottom = padding.calculateBottomPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Broadcast members", style = MaterialTheme.typography.headlineMedium)
            }
        }
        item {
            Card {
                ListItem(
                    headlineContent = { Text("${members.size} unique customers") },
                    supportingContent = {
                        Text("This list is the source of truth while selecting the matching contacts in WhatsApp. A customer cannot belong to another RestoPulse batch.")
                    }
                )
            }
        }
        item {
            OutlinedButton(
                onClick = {
                    clipboard.setText(AnnotatedString(members.joinToString("\n") { "${it.name} — ${it.phoneNumber}" }))
                },
                enabled = members.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Text(" Copy verified member list")
            }
        }
        items(members, key = { it.id }) { customer ->
            Card {
                ListItem(
                    headlineContent = { Text(customer.name.ifBlank { customer.phoneNumber }) },
                    supportingContent = { Text(customer.phoneNumber) },
                    trailingContent = {
                        Button(onClick = { vm.openWhatsApp(customer) }) {
                            Icon(Icons.Default.Message, contentDescription = null)
                            Text(" WhatsApp")
                        }
                    }
                )
            }
        }
    }
}
