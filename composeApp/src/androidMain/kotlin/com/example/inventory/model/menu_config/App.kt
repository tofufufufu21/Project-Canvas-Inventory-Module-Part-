package com.example.inventory.model.menu_config

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.inventory.pos.PosMainScreen
import com.example.inventory.pos.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    posViewModel: PosViewModel,
    repo: com.example.inventory.data.model.repository.InventoryRepositoryImpl,
    inventoryScreen: @Composable () -> Unit
) {
    var selected by remember { mutableStateOf(0) }
    val items = listOf("Dashboard", "Analytics", "Inventory", "Menu Config")
    val icons = listOf(
        Icons.Default.Dashboard,
        Icons.Default.Analytics,
        Icons.Default.Inventory,
        Icons.AutoMirrored.Filled.MenuBook
    )

    val addProductViewModel: AddProductViewModel = remember { AddProductViewModelImpl(repo) } // ✅ fixed

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Machine Logo",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Handle notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { /* TODO: Handle settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Row(Modifier.fillMaxSize().padding(innerPadding)) {
            NavigationRail {
                Spacer(Modifier.weight(1f))
                items.forEachIndexed { index, item ->
                    NavigationRailItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selected == index,
                        onClick = { selected = index }
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Box(Modifier.weight(1f)) {
                when (selected) {
                    0 -> PosMainScreen(posViewModel)
                    1 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Analytics Screen")
                    }
                    2 -> inventoryScreen()
                    3 -> MenuConfigScreen(addProductViewModel)
                }
            }
        }
    }
}
