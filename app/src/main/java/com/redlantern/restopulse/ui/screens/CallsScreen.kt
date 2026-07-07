package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redlantern.restopulse.models.CallSort
import com.redlantern.restopulse.models.CallType
import com.redlantern.restopulse.ui.components.ContentCard
import com.redlantern.restopulse.ui.components.EmptyState
import com.redlantern.restopulse.utils.readable
import com.redlantern.restopulse.viewmodels.CallsViewModel
import com.redlantern.restopulse.ui.components.ScreenHeader
import com.redlantern.restopulse.ui.components.StatusPill
import com.redlantern.restopulse.ui.components.StatusTone
import com.redlantern.restopulse.ui.components.screenPadding

@Composable
fun CallsScreen(padding: PaddingValues, vm: CallsViewModel = hiltViewModel()) {
    val calls by vm.calls.collectAsState()
    val sort by vm.sort.collectAsState()
    LazyColumn(
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenHeader("Calls", "Recent conversations, missed leads, and repeat activity") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CallSort.entries, key = { it.name }, contentType = { "call-sort" }) {
                    FilterChip(selected = it == sort, onClick = { vm.sort.value = it }, label = { Text(it.label) })
                }
            }
        }
        items(calls, key = { it.id }, contentType = { "call-row" }) { call ->
            ContentCard {
                ListItem(
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    leadingContent = {
                        val icon = when (call.callType) {
                            CallType.OUTGOING -> Icons.Default.CallMade
                            CallType.MISSED, CallType.REJECTED, CallType.BLOCKED -> Icons.Default.CallMissed
                            else -> Icons.Default.CallReceived
                        }
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    headlineContent = { Text(call.callerName.ifBlank { call.phoneNumber }, style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text("${call.callType.name.lowercase()} • ${call.callDate.readable()}") },
                    trailingContent = {
                        StatusPill(
                            "${call.durationSeconds}s",
                            tone = if (call.savedToCustomer) StatusTone.Positive else StatusTone.Neutral
                        )
                    }
                )
            }
        }
        if (calls.isEmpty()) item { EmptyState("No call history", "Imported call activity will appear here.") }
    }
}
