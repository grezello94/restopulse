package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.redlantern.restopulse.ui.components.ContentCard

@Composable
fun PermissionScreen(onRequest: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Secure local access", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(
                "RestoPulse uses call logs and contacts only on this device to identify customers, prevent duplicates, and maintain history.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            ContentCard {
                Column {
                    ListItem(
                        colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        leadingContent = { Icon(Icons.Default.Call, contentDescription = null) },
                        headlineContent = { Text("Call log") },
                        supportingContent = { Text("Find repeat callers and recent activity") }
                    )
                    ListItem(
                        colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        leadingContent = { Icon(Icons.Default.Contacts, contentDescription = null) },
                        headlineContent = { Text("Contacts") },
                        supportingContent = { Text("Rename safely without duplicate entries") }
                    )
                }
            }
            Button(onClick = onRequest, modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) { Text("Grant permissions") }
        }
    }
}
