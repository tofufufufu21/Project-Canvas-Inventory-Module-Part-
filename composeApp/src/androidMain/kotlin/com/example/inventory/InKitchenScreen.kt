package com.example.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val inKitchenItems = remember { mutableStateListOf<InKitchenItem>() }
    val warehouseItems = remember { mutableStateListOf<WarehouseItem>() }
    var showPreparationDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        refreshData(repository, inKitchenItems, warehouseItems) { isLoading = it }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Header row (kept same)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Preparation List", style = MaterialTheme.typography.headlineSmall)
            Row {
                Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAEAF3)), shape = MaterialTheme.shapes.extraLarge) {
                    Text("28 Dec 22 – 10 Jan 23", color = Color.Black)
                }
                Spacer(Modifier.width(12.dp))
                Button(onClick = { showPreparationDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)), shape = MaterialTheme.shapes.extraLarge) {
                    Text("Proceed", color = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBEBEBE)), shape = MaterialTheme.shapes.extraLarge) {
                    Text("Cancel", color = Color.Black)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Choose the item you want to transfer from the warehouse to the In-kitchen stock.", color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Text("In-Kitchen", fontSize = 16.sp)
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("In-Kitchen List", fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))

                // Table header now matches reference: Product | Date | Qty | Metric | Shelf life | Unit of time | Expiry
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF4E4E4E)).padding(vertical = 10.dp, horizontal = 8.dp)) {
                    Text("Product", color = Color.White, modifier = Modifier.weight(1.4f))
                    Text("Date", color = Color.White, modifier = Modifier.weight(1.2f))
                    Text("Qty", color = Color.White, modifier = Modifier.weight(0.6f))
                    Text("Metric", color = Color.White, modifier = Modifier.weight(0.9f))
                    Text("Shelf life", color = Color.White, modifier = Modifier.weight(0.9f))
                    Text("Unit of time", color = Color.White, modifier = Modifier.weight(0.9f))
                    Text("Expiry", color = Color.White, modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(8.dp))

                when {
                    isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    inKitchenItems.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No items in In-Kitchen") }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(inKitchenItems) { item ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Product name
                                    Text(item.product_name, modifier = Modifier.weight(1.4f))

                                    // Date (transferred_at first, fallback to created_at or calculated_expiry_date)
                                    val dateText = item.transferred_at ?: item.created_at ?: item.calculated_expiry_date
                                    Text(dateText?.split("T")?.firstOrNull() ?: "—", modifier = Modifier.weight(1.2f))

                                    // Qty (current_quantity)
                                    Text("${item.current_quantity.toInt()}", modifier = Modifier.weight(0.6f))

                                    // Metric (unit)
                                    Text(item.unit ?: "—", modifier = Modifier.weight(0.9f))

                                    // Shelf life (shelf_life_value)
                                    Text(item.shelf_life_value?.toString() ?: "—", modifier = Modifier.weight(0.9f))

                                    // Unit of time (shelf_life_unit)
                                    Text(item.shelf_life_unit ?: "—", modifier = Modifier.weight(0.9f))

                                    // Expiry (show date part)
                                    val expiryText = item.calculated_expiry_date?.split("T")?.firstOrNull() ?: item.original_expiry_date ?: "—"
                                    Text(expiryText, modifier = Modifier.weight(1f))
                                }
                                Divider(color = Color.LightGray, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPreparationDialog) {
        PreparationDialog(repository = repository, items = warehouseItems, onDismiss = { showPreparationDialog = false }, onComplete = {
            showPreparationDialog = false
            scope.launch {
                try {
                    val updatedInk = repository.getAllInKitchenItems()
                    inKitchenItems.clear(); inKitchenItems.addAll(updatedInk)
                } catch (e: Exception) { e.printStackTrace() }
                try {
                    val updatedWh = repository.getAllItems()
                    warehouseItems.clear(); warehouseItems.addAll(updatedWh)
                } catch (e: Exception) { e.printStackTrace() }
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreparationDialog(
    repository: WarehouseRepository,
    items: List<WarehouseItem>,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(PreparationStep.SELECT_ITEMS) }

    val selectedItems = remember { mutableStateMapOf<String, Boolean>() }
    val quantities = remember { mutableStateMapOf<String, String>() }
    val units = remember { mutableStateMapOf<String, String>() }         // Metric (e.g., "kg", "grams")
    val shelfLifeValues = remember { mutableStateMapOf<String, String>() } // Shelf life number
    val shelfLifeUnits = remember { mutableStateMapOf<String, String>() }  // Unit of time (days/weeks/months)

    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header row
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Preparation List", style = MaterialTheme.typography.titleLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAEAF3)), shape = MaterialTheme.shapes.extraLarge) {
                            Text("28 Dec 22 – 10 Jan 23", color = Color.Black)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            when (currentStep) {
                                PreparationStep.SELECT_ITEMS -> if (selectedItems.values.any { it }) currentStep = PreparationStep.SPECIFY_QUANTITY
                                PreparationStep.SPECIFY_QUANTITY -> {
                                    if (isProcessing) return@Button
                                    isProcessing = true
                                    scope.launch {
                                        try {
                                            val selected = items.filter { selectedItems[it.product_name] == true }
                                            for (item in selected) {
                                                val qtyStr = quantities[item.product_name] ?: ""
                                                val qty = qtyStr.toDoubleOrNull() ?: continue
                                                val unit = units[item.product_name] ?: item.unit ?: "kg"
                                                val shelfVal = shelfLifeValues[item.product_name]?.toDoubleOrNull() // Double?
                                                val shelfUnit = shelfLifeUnits[item.product_name] ?: "days"

                                                try {
                                                    repository.transferToInKitchen(
                                                        warehouseItem = item,
                                                        transferQuantity = qty,
                                                        unit = unit,
                                                        shelfLifeValue = shelfVal,
                                                        shelfLifeUnit = shelfUnit,
                                                        preparationMethod = item.preparation_method ?: "Direct Open"
                                                    )
                                                } catch (e: Exception) { e.printStackTrace() }
                                            }
                                        } finally {
                                            isProcessing = false
                                            onComplete()
                                        }
                                    }
                                }
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)), shape = MaterialTheme.shapes.extraLarge) {
                            Text(if (currentStep == PreparationStep.SELECT_ITEMS) "Proceed" else if (isProcessing) "Processing..." else "Transfer", color = Color.White)
                        }

                        Spacer(Modifier.width(8.dp))
                        Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBEBEBE)), shape = MaterialTheme.shapes.extraLarge) {
                            Text("Cancel", color = Color.Black)
                        }
                    }
                }

                Divider()

                when (currentStep) {
                    PreparationStep.SELECT_ITEMS -> {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Choose the item(s) to transfer", fontSize = 14.sp)
                            Spacer(Modifier.height(12.dp))
                            Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF4E4E4E)).padding(vertical = 8.dp, horizontal = 8.dp)) {
                                        Spacer(Modifier.width(36.dp))
                                        Text("Product", color = Color.White, modifier = Modifier.weight(1.6f))
                                        Text("Date", color = Color.White, modifier = Modifier.weight(1f))
                                        Text("Qty", color = Color.White, modifier = Modifier.weight(0.6f))
                                        Text("Category", color = Color.White, modifier = Modifier.weight(1f))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    LazyColumn {
                                        items(items) { item ->
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = selectedItems[item.product_name] ?: false, onCheckedChange = { checked ->
                                                    selectedItems[item.product_name] = checked
                                                    if (checked) {
                                                        units[item.product_name] = units[item.product_name] ?: item.unit ?: "kg"
                                                        quantities[item.product_name] = quantities[item.product_name] ?: ""
                                                        shelfLifeValues[item.product_name] = shelfLifeValues[item.product_name] ?: ""
                                                        shelfLifeUnits[item.product_name] = shelfLifeUnits[item.product_name] ?: "days"
                                                    } else {
                                                        quantities.remove(item.product_name); units.remove(item.product_name); shelfLifeValues.remove(item.product_name); shelfLifeUnits.remove(item.product_name)
                                                    }
                                                })

                                                Text(item.product_name, modifier = Modifier.weight(1.6f))
                                                Text(item.date_created?.split("T")?.firstOrNull() ?: "—", modifier = Modifier.weight(1f))
                                                Text("${item.quantity.toInt()}", modifier = Modifier.weight(0.6f))
                                                Text(item.category_type ?: "—", modifier = Modifier.weight(1f))
                                            }
                                            Divider(color = Color.LightGray, thickness = 0.5.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PreparationStep.SPECIFY_QUANTITY -> {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Set quantity and shelf-life for each selected item", fontSize = 14.sp)
                            Spacer(Modifier.height(12.dp))
                            Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // header: Product | Qty to transfer | Metric | Shelf life | Unit of time
                                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF4E4E4E)).padding(vertical = 8.dp, horizontal = 8.dp)) {
                                        Text("Product", color = Color.White, modifier = Modifier.weight(1.4f))
                                        Text("Qty to transfer", color = Color.White, modifier = Modifier.weight(0.8f))
                                        Text("Metric", color = Color.White, modifier = Modifier.weight(0.8f))
                                        Text("Shelf life", color = Color.White, modifier = Modifier.weight(0.8f))
                                        Text("Unit of time", color = Color.White, modifier = Modifier.weight(0.8f))
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    val selectedList = items.filter { selectedItems[it.product_name] == true }
                                    LazyColumn {
                                        items(selectedList) { item ->
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text(item.product_name, modifier = Modifier.weight(1.4f))

                                                OutlinedTextField(value = quantities[item.product_name] ?: "", onValueChange = { quantities[item.product_name] = it }, modifier = Modifier.weight(0.8f), singleLine = true, label = { Text("Qty") })

                                                OutlinedTextField(value = units[item.product_name] ?: item.unit ?: "kg", onValueChange = { units[item.product_name] = it }, modifier = Modifier.weight(0.8f), singleLine = true, label = { Text("Metric (e.g. kg)") })

                                                OutlinedTextField(value = shelfLifeValues[item.product_name] ?: "", onValueChange = { shelfLifeValues[item.product_name] = it }, modifier = Modifier.weight(0.8f), singleLine = true, label = { Text("Shelf value") })

                                                OutlinedTextField(value = shelfLifeUnits[item.product_name] ?: "days", onValueChange = { shelfLifeUnits[item.product_name] = it }, modifier = Modifier.weight(0.8f), singleLine = true, label = { Text("Unit of time (days)") })
                                            }
                                            Divider(color = Color.LightGray, thickness = 0.5.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Helper to refresh both tables (InKitchen + Warehouse) */
private suspend fun refreshData(
    repository: WarehouseRepository,
    inKitchenItems: MutableList<InKitchenItem>,
    warehouseItems: MutableList<WarehouseItem>,
    setLoading: (Boolean) -> Unit
) {
    setLoading(true)
    try {
        val ink = repository.getAllInKitchenItems()
        val wh = repository.getAllItems()
        inKitchenItems.clear(); inKitchenItems.addAll(ink)
        warehouseItems.clear(); warehouseItems.addAll(wh)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        setLoading(false)
    }
}
