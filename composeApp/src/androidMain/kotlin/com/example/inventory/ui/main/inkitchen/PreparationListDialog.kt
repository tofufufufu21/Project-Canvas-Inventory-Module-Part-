package com.example.inventory.ui.main.inkitchen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.inventory.data.model.WarehouseItem
import com.example.inventory.ui.main.warehouse.WarehouseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreparationListDialog(
    warehouseItems: List<WarehouseItem>,
    onDismiss: () -> Unit,
    onTransferComplete: () -> Unit,
    warehouseViewModel: WarehouseViewModel,
    inKitchenViewModel: InKitchenViewModel
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(0) }
    val selectedItems = remember { mutableStateListOf<WarehouseItem>() }

    data class Config(
        var transferQuantity: String = "1",
        var unit: String = "Pc",
        var servingSize: String = "",
        var shelfLifeValue: String = "",
        var shelfLifeUnit: String = "days",
        var useManufacturerExpiry: Boolean = false,
        var expiryIso: String? = null
    )

    val configMap = remember { mutableStateMapOf<Long, MutableState<Config>>() }

    fun ensureConfig(item: WarehouseItem) {
        val id = item.id ?: return
        if (!configMap.containsKey(id)) {
            configMap[id] = mutableStateOf(
                Config(
                    transferQuantity = "1",
                    unit = item.unit ?: "Pc",
                    servingSize = item.serving_size?.toString() ?: "",
                    expiryIso = item.expiry_date
                )
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = when (step) {
                        0 -> "Preparation List"
                        1 -> "Specify Quantity & Shelf Life"
                        2 -> "Review & Confirm"
                        else -> "Transfer Successful"
                    },
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(12.dp))

                when (step) {
                    // Step 0: Select items
                    0 -> {
                        if (warehouseItems.isEmpty()) {
                            Text("No available items to transfer.")
                        } else {
                            Column(modifier = Modifier.fillMaxHeight(0.75f)) {
                                warehouseItems.forEach { item ->
                                    val isSelected = selectedItems.contains(item)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .toggleable(
                                                value = isSelected,
                                                onValueChange = {
                                                    if (it) {
                                                        selectedItems.add(item)
                                                        ensureConfig(item)
                                                    } else selectedItems.remove(item)
                                                }
                                            ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.product_name, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                "${item.quantity} ${item.unit ?: ""}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Checkbox(checked = isSelected, onCheckedChange = null)
                                    }
                                    Divider()
                                }
                            }
                        }
                    }

                    // Step 1: Configuration
                    1 -> {
                        if (selectedItems.isEmpty()) {
                            Text("No items selected.")
                        } else {
                            val unitOptions = listOf("hours", "days", "weeks", "months")

                            Column(modifier = Modifier.fillMaxHeight(0.75f)) {
                                selectedItems.forEach { item ->
                                    val id = item.id ?: return@forEach
                                    val cfgState = configMap[id] ?: return@forEach
                                    val cfg = cfgState.value
                                    var expanded by remember { mutableStateOf(false) }

                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                        Text(item.product_name, style = MaterialTheme.typography.titleMedium)

                                        Spacer(Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = cfg.transferQuantity,
                                                onValueChange = {
                                                    cfgState.value = cfg.copy(
                                                        transferQuantity = it.filter { ch -> ch.isDigit() || ch == '.' }
                                                    )
                                                },
                                                label = { Text("Quantity") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            OutlinedTextField(
                                                value = cfg.unit,
                                                onValueChange = { cfgState.value = cfg.copy(unit = it) },
                                                label = { Text("Unit") },
                                                modifier = Modifier.width(110.dp)
                                            )
                                        }

                                        Spacer(Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = cfg.servingSize,
                                            onValueChange = {
                                                cfgState.value = cfg.copy(
                                                    servingSize = it.filter { ch -> ch.isDigit() || ch == '.' }
                                                )
                                            },
                                            label = { Text("Serving Size") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = cfg.shelfLifeValue,
                                                onValueChange = {
                                                    if (!cfg.useManufacturerExpiry) {
                                                        cfgState.value = cfg.copy(
                                                            shelfLifeValue = it.filter { ch -> ch.isDigit() }
                                                        )
                                                    }
                                                },
                                                label = { Text("Shelf Life Value") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                enabled = !cfg.useManufacturerExpiry,
                                                modifier = Modifier.weight(1f)
                                            )

                                            Spacer(Modifier.width(8.dp))
                                            Box(modifier = Modifier.weight(1f)) {
                                                OutlinedTextField(
                                                    value = cfg.shelfLifeUnit,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Unit") },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { expanded = true }
                                                )
                                                DropdownMenu(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false }
                                                ) {
                                                    unitOptions.forEach { unit ->
                                                        DropdownMenuItem(
                                                            text = { Text(unit) },
                                                            onClick = {
                                                                cfgState.value = cfg.copy(shelfLifeUnit = unit)
                                                                expanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = cfg.useManufacturerExpiry,
                                                onCheckedChange = {
                                                    cfgState.value = cfg.copy(
                                                        useManufacturerExpiry = it,
                                                        shelfLifeValue = if (it) "" else cfg.shelfLifeValue,
                                                        expiryIso = if (it) item.expiry_date else cfg.expiryIso
                                                    )
                                                }
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text("Use Manufacturer Expiry")
                                        }

                                        Text(
                                            "Original expiry: ${item.expiry_date ?: "N/A"}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Divider()
                                    }
                                }
                            }
                        }
                    }

                    // Step 2: Review
                    2 -> {
                        if (selectedItems.isEmpty()) Text("No items selected.")
                        else {
                            Column(modifier = Modifier.fillMaxHeight(0.75f)) {
                                selectedItems.forEach { item ->
                                    val id = item.id ?: return@forEach
                                    val cfg = configMap[id]?.value ?: return@forEach
                                    Text(
                                        "${item.product_name} — ${cfg.transferQuantity} ${cfg.unit} | Shelf: ${cfg.shelfLifeValue.ifBlank { "-" }} ${cfg.shelfLifeUnit} | Serving: ${cfg.servingSize}"
                                    )
                                    Divider()
                                }
                            }
                        }
                    }

                    // Step 3: Success
                    3 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("✅ Transfer Successful", style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(8.dp))
                            Text("Transferred items are now in In-Kitchen and logged in history.")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    when (step) {
                        0 -> {
                            TextButton(onClick = onDismiss) { Text("Cancel") }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { if (selectedItems.isNotEmpty()) step = 1 }) { Text("Next") }
                        }

                        1 -> {
                            TextButton(onClick = { step = 0 }) { Text("Back") }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { step = 2 }) { Text("Next") }
                        }

                        2 -> {
                            TextButton(onClick = { step = 1 }) { Text("Back") }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                scope.launch {
                                    for (item in selectedItems) {
                                        val id = item.id ?: continue
                                        val cfg = configMap[id]?.value ?: continue

                                        inKitchenViewModel.transferItemFromWarehouse(
                                            warehouseItem = item,
                                            transferQuantity = cfg.transferQuantity.toDoubleOrNull() ?: 1.0,
                                            unit = cfg.unit.ifBlank { item.unit ?: "Pc" },
                                            shelfLifeValue = cfg.shelfLifeValue.toDoubleOrNull(),
                                            shelfLifeUnit = cfg.shelfLifeUnit,
                                            preparationMethod = item.preparation_method ?: "Direct Open",
                                            expiryIso = if (cfg.useManufacturerExpiry) item.expiry_date else cfg.expiryIso,
                                            servingSize = cfg.servingSize.toDoubleOrNull(),
                                            useManufacturerExpiry = cfg.useManufacturerExpiry
                                        )
                                    }
                                    inKitchenViewModel.loadItems()
                                    warehouseViewModel.loadItems()
                                    step = 3
                                    onTransferComplete()
                                }
                            }) { Text("Confirm Transfer") }
                        }

                        3 -> TextButton(onClick = onDismiss) { Text("Close") }
                    }
                }
            }
        }
    }
}
