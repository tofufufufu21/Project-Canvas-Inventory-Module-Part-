package com.example.inventory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MenuBook
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.inventory.pos.PosMainScreen
import com.example.inventory.pos.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(posViewModel: PosViewModel, inventoryScreen: @Composable () -> Unit) {
    var selected by remember { mutableStateOf(0) }
    val items = listOf("Dashboard", "Analytics", "Inventory", "Menu Config")
    val icons: List<ImageVector> = listOf(
        Icons.Default.Dashboard, 
        Icons.Default.Analytics, 
        Icons.Default.Inventory, 
        Icons.Default.MenuBook
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* No title */ },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Machine Logo",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Handle notifications */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                    IconButton(onClick = { /* TODO: Handle settings */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
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
                    1 -> AnalyticsScreenContent()
                    2 -> inventoryScreen()
                    3 -> MenuConfigScreen()
                }
            }
        }
    }
}