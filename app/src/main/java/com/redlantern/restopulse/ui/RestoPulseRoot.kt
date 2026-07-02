package com.redlantern.restopulse.ui

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.redlantern.restopulse.permissions.PermissionManager
import com.redlantern.restopulse.ui.navigation.Route
import com.redlantern.restopulse.ui.screens.AnalyticsScreen
import com.redlantern.restopulse.ui.screens.CallsScreen
import com.redlantern.restopulse.ui.screens.CustomerDetailScreen
import com.redlantern.restopulse.ui.screens.CustomersScreen
import com.redlantern.restopulse.ui.screens.DashboardScreen
import com.redlantern.restopulse.ui.screens.GroupsScreen
import com.redlantern.restopulse.ui.screens.PermissionScreen
import com.redlantern.restopulse.ui.screens.SettingsScreen

private data class NavItem(val route: Route, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun RestoPulseRoot() {
    val scheduler = hiltViewModel<com.redlantern.restopulse.viewmodels.WorkSchedulerViewModel>().scheduler
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(PermissionManager.requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        granted = PermissionManager.requiredPermissions.all { result[it] == true || ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    }
    LaunchedEffect(granted) {
        if (granted) {
            scheduler.enqueueInitialImport()
            scheduler.schedulePeriodicSync()
        }
    }
    if (!granted) {
        PermissionScreen(onRequest = { launcher.launch(PermissionManager.requiredPermissions.toTypedArray()) })
        return
    }

    val navController = rememberNavController()
    val items = listOf(
        NavItem(Route.Dashboard, "Home", Icons.Default.Home),
        NavItem(Route.Customers, "Customers", Icons.Default.People),
        NavItem(Route.Calls, "Calls", Icons.Default.Phone),
        NavItem(Route.Groups, "Groups", Icons.Default.Groups),
        NavItem(Route.Analytics, "Stats", Icons.Default.Analytics),
        NavItem(Route.Settings, "Settings", Icons.Default.Settings)
    )
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val backStack by navController.currentBackStackEntryAsState()
                val current = backStack?.destination
                items.forEach { item ->
                    NavigationBarItem(
                        selected = current?.hierarchy?.any { it.route == item.route.path } == true,
                        onClick = {
                            navController.navigate(item.route.path) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = Route.Dashboard.path) {
            composable(Route.Dashboard.path) { DashboardScreen(padding, onCustomer = { navController.navigate(Route.Customers.path) }) }
            composable(Route.Customers.path) { CustomersScreen(padding, onOpen = { navController.navigate(Route.CustomerDetail.create(it)) }) }
            composable(Route.Calls.path) { CallsScreen(padding) }
            composable(Route.Groups.path) { GroupsScreen(padding) }
            composable(Route.Analytics.path) { AnalyticsScreen(padding) }
            composable(Route.Settings.path) { SettingsScreen(padding) }
            composable(
                Route.CustomerDetail.path,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { CustomerDetailScreen(padding, onBack = { navController.popBackStack() }) }
        }
    }
}
