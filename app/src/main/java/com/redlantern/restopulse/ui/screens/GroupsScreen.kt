package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redlantern.restopulse.viewmodels.GroupsViewModel

@Composable
fun GroupsScreen(padding: PaddingValues, vm: GroupsViewModel = hiltViewModel()) {
    val groups by vm.groups.collectAsState()
    LazyColumn(
        contentPadding = PaddingValues(top = padding.calculateTopPadding() + 16.dp, bottom = padding.calculateBottomPadding() + 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Marketing Groups") }
        items(groups, key = { it.id }) {
            Card { ListItem(headlineContent = { Text(it.name) }, supportingContent = { Text("Maximum size ${it.maxSize}") }) }
        }
        if (groups.isEmpty()) item { Text("Groups are created automatically as new customers are added.") }
    }
}
