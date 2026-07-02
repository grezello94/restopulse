package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redlantern.restopulse.models.CustomerFilter
import com.redlantern.restopulse.viewmodels.CustomersViewModel

@Composable
fun CustomersScreen(padding: PaddingValues, onOpen: (Long) -> Unit, vm: CustomersViewModel = hiltViewModel()) {
    val customers by vm.customers.collectAsState()
    val query by vm.query.collectAsState()
    val filter by vm.filter.collectAsState()
    LazyColumn(
        contentPadding = PaddingValues(top = padding.calculateTopPadding() + 16.dp, bottom = padding.calculateBottomPadding() + 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Search phone, name, tag, notes, location") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CustomerFilter.entries) {
                    AssistChip(onClick = { vm.filter.value = it }, label = { Text(it.label) }, enabled = it != filter)
                }
            }
        }
        items(customers, key = { it.id }) { customer ->
            Card(Modifier.fillMaxWidth().clickable { onOpen(customer.id) }) {
                ListItem(
                    headlineContent = { Text(customer.name.ifBlank { customer.phoneNumber }) },
                    supportingContent = { Text("${customer.normalizedNumber} • ${customer.customerTag.ifBlank { "No tag" }} • ${customer.totalCalls} calls") },
                    trailingContent = { Text(if (customer.whatsappAvailable) "WhatsApp" else "No WA") }
                )
            }
        }
        if (customers.isEmpty()) item { Text("No customers match this view.", Modifier.padding(8.dp)) }
    }
}
