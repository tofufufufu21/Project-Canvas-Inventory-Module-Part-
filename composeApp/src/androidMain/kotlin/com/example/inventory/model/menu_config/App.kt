package com.example.inventory.model.menu_config

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inventory.pos.PosMainScreen
import com.example.inventory.pos.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    posViewModel: PosViewModel,
    repo: com.example.inventory.data.model.repository.InventoryRepositoryImpl,
    inventoryScreen: @Composable () -> Unit,
    onLogout: () -> Unit = {}
) {
    var selected by remember { mutableStateOf(0) }

    val items = listOf("Dashboard", "Analytics", "Inventory", "Menu Config")
    val icons = listOf(
        Icons.Default.Dashboard,
        Icons.Default.Analytics,
        Icons.Default.Storage,                 // safer inventory icon
        Icons.AutoMirrored.Filled.MenuBook
    )

    val addProductViewModel: AddProductViewModel = remember { AddProductViewModelImpl(repo) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "POS Logo",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sidebar Navigation (no top logo)
            NavigationRail(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(96.dp)
                    .padding(vertical = 8.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                // Top-aligned nav items
                items.forEachIndexed { index, item ->
                    NavigationRailItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selected == index,
                        onClick = { selected = index },
                        alwaysShowLabel = true
                    )
                }

                // push logout to bottom
                Spacer(Modifier.weight(1f))
                Divider()
                NavigationRailItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = { onLogout() },
                    alwaysShowLabel = true
                )
            }

            // Main content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (selected) {
                    0 -> PosMainScreen(posViewModel)
                    1 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Analytics Screen")
                    }
                    2 -> {
                        // Inventory screen (provided by MainActivity) — ensure this is invoked
                        inventoryScreen()
                    }
                    3 -> MenuConfigScreen(addProductViewModel)
                }
            }
        }
    }
}
