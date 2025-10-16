package com.example.inventory.ui.main.inkitchen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inventory.data.model.InKitchenItem
import com.example.inventory.ui.main.warehouse.WarehouseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InKitchenScreen(
    viewModel: InKitchenViewModel,
    warehouseViewModel: WarehouseViewModel
) {
    val inKitchenItems by viewModel.inKitchenItems.collectAsState()
    val warehouseItems by warehouseViewModel.warehouseItems.collectAsState() // ✅ fixed

    var showPreparation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadItems()
        warehouseViewModel.loadItems()
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("In-Kitchen Items", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { showPreparation = true }) {
                Icon(Icons.Default.Check, contentDescription = "Prepare")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Preparation List")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (inKitchenItems.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No in-kitchen items")
            }
        } else {
            LazyColumn {
                items(inKitchenItems) { item ->
                    InKitchenRow(item)
                    Divider()
                }
            }
        }
    }

    if (showPreparation) {
        PreparationListDialog(
            warehouseItems = warehouseItems,
            onDismiss = { showPreparation = false },
            onTransferComplete = {
                viewModel.loadItems()
                warehouseViewModel.loadItems()
            },
            warehouseViewModel = warehouseViewModel,
            inKitchenViewModel = viewModel
        )
    }
}

@Composable
fun InKitchenRow(item: InKitchenItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(item.product_name)
        Text("${item.current_quantity} ${item.unit ?: ""}")
        Text(item.status)
    }
}
