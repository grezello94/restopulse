package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redlantern.restopulse.models.CallSort
import com.redlantern.restopulse.utils.readable
import com.redlantern.restopulse.viewmodels.CallsViewModel

@Composable
fun CallsScreen(padding: PaddingValues, vm: CallsViewModel = hiltViewModel()) {
    val calls by vm.calls.collectAsState()
    val sort by vm.sort.collectAsState()
    LazyColumn(
        contentPadding = PaddingValues(top = padding.calculateTopPadding() + 16.dp, bottom = padding.calculateBottomPadding() + 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CallSort.entries) {
                    AssistChip(onClick = { vm.sort.value = it }, label = { Text(it.label) }, enabled = it != sort)
                }
            }
        }
        items(calls, key = { it.id }) { call ->
            Card {
                ListItem(
                    headlineContent = { Text(call.callerName.ifBlank { call.phoneNumber }) },
                    supportingContent = { Text("${call.callType.name.lowercase()} • ${call.callDate.readable()}") },
                    trailingContent = { Text("${call.durationSeconds}s") }
                )
            }
        }
        if (calls.isEmpty()) item { Text("No call history has been imported yet.") }
    }
}
