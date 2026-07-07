package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import com.redlantern.restopulse.ui.components.ContentCard
import com.redlantern.restopulse.ui.components.EmptyState
import com.redlantern.restopulse.ui.components.ScreenHeader
import com.redlantern.restopulse.ui.components.StatusPill
import com.redlantern.restopulse.ui.components.StatusTone
import com.redlantern.restopulse.ui.components.screenPadding

@Composable
fun CustomersScreen(padding: PaddingValues, onOpen: (Long) -> Unit, vm: CustomersViewModel = hiltViewModel()) {
    val customers by vm.customers.collectAsState()
    val query by vm.query.collectAsState()
    val filter by vm.filter.collectAsState()
    LazyColumn(
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenHeader("Customers", "Search, segment, and open every guest profile") }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search customers") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CustomerFilter.entries, key = { it.name }, contentType = { "customer-filter" }) {
                    FilterChip(selected = it == filter, onClick = { vm.filter.value = it }, label = { Text(it.label) })
                }
            }
        }
        items(customers, key = { it.id }, contentType = { "customer-row" }) { customer ->
            ContentCard(Modifier.clickable { onOpen(customer.id) }) {
                ListItem(
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    leadingContent = {
                        if (customer.favorite || customer.vip) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        }
                    },
                    headlineContent = { Text(customer.name.ifBlank { customer.phoneNumber }, style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text("${customer.normalizedNumber} • ${customer.customerTag.ifBlank { "No tag" }} • ${customer.totalCalls} calls") },
                    trailingContent = {
                        StatusPill(
                            if (customer.whatsappAvailable) "WhatsApp" else "Phone",
                            tone = if (customer.whatsappAvailable) StatusTone.Positive else StatusTone.Neutral
                        )
                    }
                )
            }
        }
        if (customers.isEmpty()) item { EmptyState("No customers found", "Try another search or filter.") }
    }
}
