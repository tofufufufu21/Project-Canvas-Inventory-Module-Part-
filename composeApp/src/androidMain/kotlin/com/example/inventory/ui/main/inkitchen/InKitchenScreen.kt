package com.example.inventory.ui.main.inkitchen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.model.InKitchenItem
import com.example.inventory.ui.main.warehouse.WarehouseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InKitchenScreen(
    viewModel: InKitchenViewModel,
    warehouseViewModel: WarehouseViewModel
) {
    val inKitchenItems by viewModel.inKitchenItems.collectAsState()
    val warehouseItems by warehouseViewModel.warehouseItems.collectAsState()

    var showPreparation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadItems()
        warehouseViewModel.loadItems()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // ✅ Header Row with working Preparation List Button (DO NOT TOUCH)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "In-Kitchen Items",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Button(onClick = { showPreparation = true }) {
                Icon(Icons.Default.Check, contentDescription = "Prepare")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Preparation List")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0E0E0))
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Product Name", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold)
            Text("Quantity", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Serving Size", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Expiry", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Status", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        }

        Divider(color = Color.Gray, thickness = 1.dp)

        // ✅ Table Content
        if (inKitchenItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No in-kitchen items available", color = Color.Gray)
            }
        } else {
            LazyColumn {
                items(inKitchenItems) { item ->
                    InKitchenRow(item)
                    Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                }
            }
        }
    }

    // ✅ This remains untouched — your Preparation List dialog still works
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

// ✅ Row layout for each In-Kitchen item (table-style)
@Composable
fun InKitchenRow(item: InKitchenItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(item.product_name, modifier = Modifier.weight(1.3f))
        Text("${item.current_quantity} ${item.unit ?: ""}", modifier = Modifier.weight(1f))
        Text(item.serving_size?.toString() ?: "-", modifier = Modifier.weight(1f))
        Text(item.calculated_expiry_date ?: "-", modifier = Modifier.weight(1f))
        Text(
            text = item.status ?: "-",
            color = if (item.status == "available") Color(0xFF2E7D32) else Color.Red,
            modifier = Modifier.weight(1f)
        )
    }
}
