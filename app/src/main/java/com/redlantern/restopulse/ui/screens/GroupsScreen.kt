package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redlantern.restopulse.viewmodels.GroupsViewModel
import com.redlantern.restopulse.ui.components.ContentCard
import com.redlantern.restopulse.ui.components.EmptyState
import com.redlantern.restopulse.ui.components.ScreenHeader
import com.redlantern.restopulse.ui.components.StatusPill
import com.redlantern.restopulse.ui.components.screenPadding

@Composable
fun GroupsScreen(padding: PaddingValues, vm: GroupsViewModel = hiltViewModel()) {
    val groups by vm.groups.collectAsState()
    LazyColumn(
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenHeader("Marketing Groups", "Automatically batched as your customer base grows") }
        items(groups, key = { it.id }, contentType = { "group-row" }) {
            ContentCard {
                ListItem(
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    leadingContent = { Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    headlineContent = { Text(it.name, style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text("Ready for broadcast segmentation") },
                    trailingContent = { StatusPill("${it.maxSize} max") }
                )
            }
        }
        if (groups.isEmpty()) item { EmptyState("No groups yet", "Groups are created automatically as new customers are added.") }
    }
}
