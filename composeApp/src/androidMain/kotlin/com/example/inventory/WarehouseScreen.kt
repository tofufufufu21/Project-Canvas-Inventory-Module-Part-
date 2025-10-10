package com.example.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseScreen(repository: WarehouseRepository) {
    val scope = rememberCoroutineScope()
    val itemsList = remember { mutableStateListOf<WarehouseItem>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Warehouse") }

    // ✅ Load Supabase Data
    LaunchedEffect(Unit) {
        try {
            val fetchedItems = repository.getAllItems()
            itemsList.clear()
            itemsList.addAll(fetchedItems)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            // ✅ Header Row (Title + Date + Add Stock)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Inventory",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Date Range Filter (static placeholder)
                    Button(
                        onClick = { /* Future filter */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAEAF3)),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text(
                            "28 Dec 22 – 10 Jan 23",
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // ✅ Add Stock Button (Popup)
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBEBEBE)),
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            "+ Add Stock",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Tabs (Warehouse / In-Kitchen)
            Row {
                listOf("Warehouse", "In-Kitchen").forEach { tab ->
                    Text(
                        text = tab,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == tab) Color.Black else Color.Gray,
                        modifier = Modifier
                            .padding(end = 20.dp)
                            .clickable { selectedTab = tab }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF4E4E4E))
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Product Name", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1.5f))
                Text("Date Created", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1.2f))
                Text("Quantity", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text("Type of Product", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1.2f))
                Text("Category of Product", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1.2f))
                Text("Expiry", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            // ✅ Items Table
            if (itemsList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No items found.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(itemsList) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(item.product_name, modifier = Modifier.weight(1.5f))
                            Text(
                                item.date_created?.split(" ")?.firstOrNull() ?: "—",
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(item.quantity.toString(), modifier = Modifier.weight(1f))
                            Text(item.category_type, modifier = Modifier.weight(1.2f))
                            Text(item.sub_category ?: "—", modifier = Modifier.weight(1.2f))
                            Text(item.expiry_date ?: "—", modifier = Modifier.weight(1f))
                        }
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }
        }
    }

    // ✅ Popup Dialog for Add Stock
    if (showAddDialog) {
        AddStockDialog(
            repository = repository,
            onDismiss = { showAddDialog = false },
            onItemAdded = {
                showAddDialog = false
                scope.launch {
                    val updatedItems = repository.getAllItems()
                    itemsList.clear()
                    itemsList.addAll(updatedItems)
                }
            }
        )
    }
}
