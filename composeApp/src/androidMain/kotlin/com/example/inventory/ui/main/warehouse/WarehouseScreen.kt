package com.example.inventory.ui.main.warehouse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventory.data.model.WarehouseItem
import com.example.inventory.ui.main.inkitchen.InKitchenScreen
import com.example.inventory.ui.main.inkitchen.InKitchenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseScreen(
    warehouseViewModel: WarehouseViewModel,
    inKitchenViewModel: InKitchenViewModel
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Warehouse", "In-Kitchen")

    val warehouseItems by warehouseViewModel.warehouseItems.collectAsState()
    val inKitchenItems by inKitchenViewModel.inKitchenItems.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // ✅ Load data once
    LaunchedEffect(Unit) {
        warehouseViewModel.loadItems()
        inKitchenViewModel.loadItems()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Inventory Module") }) },
        floatingActionButton = {
            if (tabIndex == 0) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
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
                        text = { Text(title, modifier = Modifier.padding(10.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (tabIndex) {
                0 -> WarehouseTable(warehouseItems)
                1 -> InKitchenScreen(
                    viewModel = inKitchenViewModel,
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
private fun WarehouseTable(items: List<WarehouseItem>) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No warehouse items found.")
        }
    } else {
        Column {
            // ✅ Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderCell("Product Name", 1.5f)
                HeaderCell("Date Created", 1.2f)
                HeaderCell("Quantity", 1f)
                HeaderCell("Type", 1f)
                HeaderCell("Category", 1f)
                HeaderCell("Expiry", 1f)
                HeaderCell("Action", 0.8f)
            }

            HorizontalDivider(color = Color.Gray, thickness = 1.dp)
            Spacer(Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(1.dp, RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                items(items) { item ->
                    WarehouseRow(item)
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                }
            }
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.Black,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 4.dp)
    )
}

@Composable
private fun WarehouseRow(item: WarehouseItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TableCell(item.product_name, 1.5f)
        TableCell(item.date_created ?: "N/A", 1.2f) // ✅ Use actual field name
        TableCell("${item.quantity ?: 0.0} ${item.unit ?: ""}", 1f)
        TableCell(item.category_type ?: "-", 1f)
        TableCell(item.sub_category ?: "-", 1f)
        TableCell(item.expiry_date ?: "N/A", 1f)
        TableActionCell()
    }
}

@Composable
private fun RowScope.TableCell(text: String, weight: Float) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = Color.DarkGray,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 4.dp)
    )
}

@Composable
private fun TableActionCell() {
    Button(
        onClick = { /* TODO: Future functionality */ },
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.2f)),
        modifier = Modifier
            .height(35.dp)
    ) {
        Text("⋯", color = Color.Black, fontWeight = FontWeight.Bold)
    }
}
