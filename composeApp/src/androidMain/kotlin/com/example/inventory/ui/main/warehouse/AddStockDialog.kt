package com.example.inventory.ui.main.warehouse

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.inventory.data.model.WarehouseItem
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddStockDialog(
    onDismiss: () -> Unit,
    onItemAdded: () -> Unit,
    warehouseViewModel: WarehouseViewModel
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    // supabaseService is accessed through the viewmodel (make sure the VM exposes it)
    val supabaseService = warehouseViewModel.supabaseService

    var step by remember { mutableStateOf(0) }

    // Step 1 fields
    var productName by remember { mutableStateOf("") }
    var categoryType by remember { mutableStateOf("Edible") }
    var subCategory by remember { mutableStateOf("") }
    var productImageUrl by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Step 2 fields
    var quantity by remember { mutableStateOf("") } // user types an integer
    var unit by remember { mutableStateOf("Pc") }
    var prepMethod by remember { mutableStateOf("Direct Open") }
    var hasExpiry by remember { mutableStateOf(false) }
    var expiryDates by remember { mutableStateOf(listOf<String>()) } // dynamic list sized to quantity

    val categoryOptions = listOf("Edible", "Non-Edible")
    val subcategories = listOf("Raw", "Syrup", "Powder", "Liquid", "Dried", "Frozen", "Fresh")
    val units = listOf("Kg", "Ml", "G", "Pc", "L", "Others")
    val prepMethods = listOf("Needs to be Cooked/Prepped", "Direct Open", "Ready-to-Use")

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (step > 0) {
                        IconButton(onClick = { step-- }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                    Text(
                        text = when (step) {
                            0 -> "Add Stock"
                            1 -> "Quantity & Expiry"
                            2 -> "Preview"
                            else -> "Success"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // --- Step UIs ---
                when (step) {
                    // Step 0: Details
                    0 -> {
                        OutlinedTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            label = { Text("Product Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))
                        DropdownSelector("Category", categoryOptions, categoryType) {
                            categoryType = it
                            if (it != "Edible") subCategory = ""
                        }

                        if (categoryType == "Edible") {
                            Spacer(Modifier.height(8.dp))
                            DropdownSelector("Subcategory", subcategories, subCategory) {
                                subCategory = it
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f))
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (productImageUrl != null)
                                AsyncImage(
                                    model = productImageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            else
                                Text("Tap to Upload Image")
                        }
                    }

                    // Step 1: Quantity & expiry
                    1 -> {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = {
                                // update quantity and adjust expiryDates list length
                                quantity = it.filter { ch -> ch.isDigit() } // keep digits only
                                val q = quantity.toIntOrNull() ?: 0
                                expiryDates = if (expiryDates.size == q) {
                                    expiryDates
                                } else {
                                    // if q > current size -> expand with empty strings
                                    // if q < current size -> trim
                                    List(q) { index -> expiryDates.getOrNull(index) ?: "" }
                                }
                            },
                            label = { Text("Quantity") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        DropdownSelector("Unit", units, unit) { unit = it }

                        Spacer(Modifier.height(8.dp))

                        DropdownSelector("Preparation Method", prepMethods, prepMethod) { prepMethod = it }

                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Has Expiry?")
                            Spacer(Modifier.width(8.dp))
                            RadioButton(selected = hasExpiry, onClick = { hasExpiry = true })
                            Text("Yes")
                            Spacer(Modifier.width(8.dp))
                            RadioButton(selected = !hasExpiry, onClick = { hasExpiry = false })
                            Text("No")
                        }

                        if (hasExpiry && (expiryDates.isNotEmpty())) {
                            Spacer(Modifier.height(8.dp))
                            expiryDates.forEachIndexed { index, value ->
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { newVal ->
                                        expiryDates = expiryDates.toMutableList().apply { this[index] = newVal }
                                    },
                                    label = { Text("Expiry #${index + 1} (YYYY-MM-DD)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }

                    // Step 2: Preview
                    2 -> {
                        Text("Preview:", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Name: $productName")
                        Text("Category: $categoryType")
                        Text("Subcategory: ${if (subCategory.isBlank()) "-" else subCategory}")
                        Text("Quantity: $quantity $unit")
                        Text("Prep: $prepMethod")
                        Text("Has Expiry: ${if (hasExpiry) "Yes" else "No"}")
                        if (hasExpiry) Text("Expiry Dates: ${expiryDates.joinToString()}")
                    }

                    // Step 3: Success
                    else -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("✅ Added Successfully!", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Footer actions
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    if (step < 3) TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        when (step) {
                            0 -> {
                                // Upload image if picked, then advance
                                if (imageUri != null) {
                                    scope.launch {
                                        val uploaded = supabaseService?.let {
                                            it.uploadImage(context, imageUri!!)
                                        }
                                        // uploaded could be null; still advance
                                        productImageUrl = uploaded
                                        step++
                                    }
                                } else {
                                    step++
                                }
                            }

                            1 -> {
                                // move to preview
                                step++
                            }

                            2 -> {
                                // Finish -> add to Supabase
                                scope.launch {
                                    // current time string
                                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()

                                    val qInt = quantity.toIntOrNull() ?: 0

                                    // If user provided multiple expiry dates and qty matches that number:
                                    if (hasExpiry && expiryDates.size == qInt && qInt > 0) {
                                        // Insert N rows, each with quantity=1 and their own expiry
                                        expiryDates.forEach { expiry ->
                                            val item = WarehouseItem(
                                                product_name = productName,
                                                product_image_url = productImageUrl,
                                                category_type = categoryType,
                                                sub_category = if (categoryType == "Edible") subCategory else null,
                                                quantity = 1.0,
                                                unit = unit,
                                                preparation_method = prepMethod,
                                                has_expiry = true,
                                                expiry_date = expiry.ifBlank { null },
                                                date_created = now
                                            )
                                            warehouseViewModel.addItem(item)
                                        }
                                    } else {
                                        // Either no expiry, or one expiry for whole batch -> store as single row with aggregated quantity
                                        val item = WarehouseItem(
                                            product_name = productName,
                                            product_image_url = productImageUrl,
                                            category_type = categoryType,
                                            sub_category = if (categoryType == "Edible") subCategory else null,
                                            quantity = (quantity.toDoubleOrNull() ?: 0.0),
                                            unit = unit,
                                            preparation_method = prepMethod,
                                            has_expiry = hasExpiry,
                                            expiry_date = if (hasExpiry) expiryDates.firstOrNull() else null,
                                            date_created = now
                                        )
                                        warehouseViewModel.addItem(item)
                                    }

                                    // refresh lists (VM functions should re-fetch)
                                    warehouseViewModel.loadItems()
                                    step = 3
                                }
                            }

                            else -> {
                                onItemAdded()
                            }
                        }
                    }) {
                        Text(if (step < 2) "Next" else if (step == 2) "Finish" else "Done")
                    }
                }
            }
        }
    }
}

/**
 * Simple dropdown selector built on an OutlinedTextField + DropdownMenu
 */
@Composable
fun DropdownSelector(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Box {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                enabled = false,
                readOnly = true
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = {
                            onSelect(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
