package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redlantern.restopulse.viewmodels.GroupsViewModel

@Composable
fun GroupsScreen(
    padding: PaddingValues,
    onOpen: (Long) -> Unit,
    vm: GroupsViewModel = hiltViewModel()
) {
    val groups by vm.groups.collectAsState()
    val message by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding() + 16.dp,
            bottom = padding.calculateBottomPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Broadcast Planner", style = MaterialTheme.typography.headlineMedium) }
        item {
            Text(
                "Your full phone book is normalized by number, imported, and split into locked batches. Preparing again only assigns new contacts—never repeats existing members."
            )
        }
        item {
            Button(onClick = vm::prepareBatches, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Campaign, contentDescription = null)
                Text(" Audit contacts & prepare batches")
            }
        }
        items(groups, key = { it.id }) { group ->
            Card(Modifier.fillMaxWidth().clickable { onOpen(group.id) }) {
                ListItem(
                    headlineContent = { Text(group.name) },
                    supportingContent = { Text("Up to ${group.maxSize} unique customers • Tap to verify members") }
                )
            }
        }
        if (groups.isEmpty()) {
            item { Text("Tap Prepare to place existing customers into deduplicated batches.") }
        }
    }
    SnackbarHost(snackbar)
}
