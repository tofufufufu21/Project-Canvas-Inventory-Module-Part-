package com.example.inventory.ui.main.warehouse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inventory.data.model.WarehouseItem
import com.example.inventory.ui.main.inkitchen.InKitchenScreen
import com.example.inventory.ui.main.inkitchen.InKitchenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseScreen(
    warehouseViewModel: WarehouseViewModel,
    inKitchenViewModel: InKitchenViewModel
) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Warehouse", "In-Kitchen")

    val warehouseItems by warehouseViewModel.warehouseItems.collectAsState()
    val inKitchenItems by inKitchenViewModel.inKitchenItems.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // Load both datasets on first composition
    LaunchedEffect(Unit) {
        warehouseViewModel.loadItems()
        inKitchenViewModel.loadItems()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Inventory") })
        },
        floatingActionButton = {
            if (tabIndex == 0) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Stock")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
        ) {
            // ✅ Tabs
            TabRow(selectedTabIndex = tabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title, modifier = Modifier.padding(12.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ Tab content
            when (tabIndex) {
                0 -> WarehouseList(warehouseItems)
                1 -> InKitchenScreen(
                    inKitchenViewModel = inKitchenViewModel,
                    warehouseViewModel = warehouseViewModel
                )
            }
        }
    }

    // ✅ Add Item Dialog
    if (showAddDialog) {
        AddStockDialog(
            onDismiss = { showAddDialog = false },
            onItemAdded = {
                warehouseViewModel.loadItems()
                inKitchenViewModel.loadItems()
                showAddDialog = false
            },
            warehouseViewModel = warehouseViewModel
        )
    }
}

@Composable
private fun WarehouseList(items: List<WarehouseItem>) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No items in warehouse.")
        }
    } else {
        LazyColumn {
            items(items) { item ->
                WarehouseItemRow(item)
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun WarehouseItemRow(item: WarehouseItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(item.product_name, modifier = Modifier.weight(1f))
        Text(item.quantity.toString(), modifier = Modifier.weight(0.5f))
        Text(item.category_type, modifier = Modifier.weight(1f))
        Text(item.unit ?: "-", modifier = Modifier.weight(0.5f))
    }
}
