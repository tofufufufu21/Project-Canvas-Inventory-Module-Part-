package com.example.inventory.ui.main.inkitchen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    val selectedItems = remember { mutableStateListOf<WarehouseItem>() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Preparation List",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (warehouseItems.isEmpty()) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No available items to transfer.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(warehouseItems) { item ->
                            val isSelected = selectedItems.contains(item)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .toggleable(
                                        value = isSelected,
                                        onValueChange = { checked ->
                                            if (checked) selectedItems.add(item)
                                            else selectedItems.remove(item)
                                        }
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product_name, fontWeight = FontWeight.SemiBold)
                                    Text("${item.quantity} ${item.unit}", style = MaterialTheme.typography.bodySmall)
                                }
                                Checkbox(checked = isSelected, onCheckedChange = null)
                            }
                            HorizontalDivider()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                for (item in selectedItems) {
                                    warehouseViewModel.transferToInKitchen(
                                        warehouseItem = item,
                                        transferQuantity = item.quantity,
                                        unit = item.unit ?: "Pc",
                                        shelfLifeValue = null,
                                        shelfLifeUnit = null,
                                        preparationMethod = item.preparation_method ?: "Direct Open",
                                        expiryIso = item.expiry_date
                                    )
                                }

                                warehouseViewModel.loadItems()
                                inKitchenViewModel.loadItems()
                                onTransferComplete()
                            }
                        },
                        enabled = selectedItems.isNotEmpty()
                    ) {
                        Text("Proceed")
                    }
                }
            }
        }
    }
}
