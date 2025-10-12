package com.example.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

enum class PreparationStep {
    SELECT_ITEMS,
    SPECIFY_QUANTITY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InKitchenScreen(repository: WarehouseRepository) {
    val scope = rememberCoroutineScope()
    val warehouseItems = remember { mutableStateListOf<WarehouseItem>() }
    var showPreparationDialog by remember { mutableStateOf(false) }

    // Load warehouse items
    LaunchedEffect(Unit) {
        try {
            val items = repository.getAllItems()
            warehouseItems.clear()
            warehouseItems.addAll(items)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Preparation List",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,  // ✅ FIXED
                horizontalArrangement = Arrangement.End
            ) {
                // Date Range Filter
                Button(
                    onClick = { /* Future filter */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAEAF3)),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("28 Dec 22 – 10 Jan 23", color = Color.Black)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Proceed Button
                Button(
                    onClick = { showPreparationDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("Proceed", color = Color.White, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Cancel Button
                Button(
                    onClick = { /* Handle cancel */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBEBEBE)),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("Cancel", color = Color.Black, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Choose the item you want to transfer from the warehouse to the In-kitchen stock.",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // In-Kitchen Tab
        Text(
            "In-Kitchen",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Warehouse List Card
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Warehouse List",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4E4E4E))
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                ) {
                    Text("Product Name", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1.5f))
                    Text("Date Created", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1.2f))
                    Text("Quantity", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(0.8f))
                    Text("Type of Product", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text("Category of Product", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1.2f))
                    Text("Expiry", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Items List
                if (warehouseItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No items in warehouse")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(warehouseItems) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.product_name, modifier = Modifier.weight(1.5f))
                                Text(
                                    item.date_created?.split(" ")?.firstOrNull() ?: "—",
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text("${item.quantity.toInt()}", modifier = Modifier.weight(0.8f))
                                Text(item.category_type, modifier = Modifier.weight(1f))
                                Text(item.sub_category ?: "—", modifier = Modifier.weight(1.2f))
                                Text(item.expiry_date?.split(";")?.firstOrNull() ?: "—", modifier = Modifier.weight(1f))
                            }
                            Divider(color = Color.LightGray, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }

    // Preparation Dialog
    if (showPreparationDialog) {
        PreparationDialog(
            items = warehouseItems,
            onDismiss = { showPreparationDialog = false },
            onComplete = {
                showPreparationDialog = false
                // Refresh items or navigate
            }
        )
    }
}

@Composable
fun PreparationDialog(
    items: List<WarehouseItem>,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(PreparationStep.SELECT_ITEMS) }
    val selectedItems = remember { mutableStateMapOf<String, Boolean>() }
    val itemQuantities = remember { mutableStateMapOf<String, String>() }
    val itemMetrics = remember { mutableStateMapOf<String, String>() }
    val itemShelfLife = remember { mutableStateMapOf<String, String>() }
    val itemTimeUnits = remember { mutableStateMapOf<String, String>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Preparation List",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,  // ✅ FIXED
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { /* Date filter */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAEAF3)),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text("28 Dec 22 – 10 Jan 23", color = Color.Black)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Proceed Button
                        Button(
                            onClick = {
                                when (currentStep) {
                                    PreparationStep.SELECT_ITEMS -> {
                                        if (selectedItems.values.any { it }) {
                                            currentStep = PreparationStep.SPECIFY_QUANTITY
                                        }
                                    }
                                    PreparationStep.SPECIFY_QUANTITY -> {
                                        // Save and complete
                                        onComplete()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text("Proceed", color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBEBEBE)),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text("Cancel", color = Color.Black)
                        }
                    }
                }

                Divider()

                // Content based on step
                when (currentStep) {
                    PreparationStep.SELECT_ITEMS -> {
                        SelectItemsStep(
                            items = items,
                            selectedItems = selectedItems
                        )
                    }
                    PreparationStep.SPECIFY_QUANTITY -> {
                        SpecifyQuantityStep(
                            items = items.filter { selectedItems[it.product_name] == true },
                            quantities = itemQuantities,
                            metrics = itemMetrics,
                            shelfLife = itemShelfLife,
                            timeUnits = itemTimeUnits
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectItemsStep(
    items: List<WarehouseItem>,
    selectedItems: MutableMap<String, Boolean>
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Choose the item you want to transfer from the warehouse to the In-kitchen stock.",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("In-Kitchen", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Warehouse List Card
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Warehouse List", fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4E4E4E))
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.width(40.dp)) // Checkbox space
                    Text("Product Name", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                    Text("Date Created", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("Quantity", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(0.6f))
                    Text("Type of Product", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                    Text("Category of Product", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("Expiry", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Items with checkboxes
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedItems[item.product_name] ?: false,
                                onCheckedChange = { isChecked ->
                                    selectedItems[item.product_name] = isChecked
                                }
                            )
                            Text(item.product_name, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                            Text(
                                item.date_created?.split(" ")?.get(0) ?: "—",
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text("${item.quantity.toInt()}", fontSize = 12.sp, modifier = Modifier.weight(0.6f))
                            Text(item.category_type, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                            Text(item.sub_category ?: "—", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(item.expiry_date?.split(";")?.firstOrNull() ?: "—", fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                        }
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecifyQuantityStep(
    items: List<WarehouseItem>,
    quantities: MutableMap<String, String>,
    metrics: MutableMap<String, String>,
    shelfLife: MutableMap<String, String>,
    timeUnits: MutableMap<String, String>
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Enter how many units you want to transfer. Make sure one quantity does not exceed the available stock.",
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            "Set the shelf-life for the transferred stock. This helps track freshness and expiry alerts.",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("In-Kitchen", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Specify Quantity and Shelf life", fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4E4E4E))
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                ) {
                    Text("Product Name", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                    Text("Quantity", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                    Text("Metric", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                    Text("Shelf Life", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                    Text("Unit of time", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                    Text("Expiry", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.product_name, fontSize = 12.sp, modifier = Modifier.weight(1.2f))

                            // Quantity Input
                            OutlinedTextField(
                                value = quantities[item.product_name] ?: "",
                                onValueChange = { quantities[item.product_name] = it },
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(50.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            // Metric Dropdown
                            var metricExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = metricExpanded,
                                onExpandedChange = { metricExpanded = it },
                                modifier = Modifier.weight(0.8f)
                            ) {
                                OutlinedTextField(
                                    value = metrics[item.product_name] ?: "kg",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .menuAnchor()
                                        .height(50.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = metricExpanded,
                                    onDismissRequest = { metricExpanded = false }
                                ) {
                                    listOf("kg", "g", "pcs", "L", "ml").forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text(unit) },
                                            onClick = {
                                                metrics[item.product_name] = unit
                                                metricExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Shelf Life Input
                            OutlinedTextField(
                                value = shelfLife[item.product_name] ?: "",
                                onValueChange = { shelfLife[item.product_name] = it },
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(50.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            // Time Unit Dropdown
                            var timeUnitExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = timeUnitExpanded,
                                onExpandedChange = { timeUnitExpanded = it },
                                modifier = Modifier.weight(0.8f)
                            ) {
                                OutlinedTextField(
                                    value = timeUnits[item.product_name] ?: "days",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .menuAnchor()
                                        .height(50.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = timeUnitExpanded,
                                    onDismissRequest = { timeUnitExpanded = false }
                                ) {
                                    listOf("days", "weeks", "months", "years").forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text(unit) },
                                            onClick = {
                                                timeUnits[item.product_name] = unit
                                                timeUnitExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                item.expiry_date?.split(";")?.firstOrNull() ?: "—",
                                fontSize = 12.sp,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}