package com.example.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Enum to represent the steps in the Add Stock flow
enum class AddStockStep {
    PRODUCT_DETAILS,
    QUANTITY_EXPIRY,
    PREVIEW,
    SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddStockDialog(
    repository: WarehouseRepository,
    onDismiss: () -> Unit,
    onItemAdded: () -> Unit
) {
    var currentStep by remember { mutableStateOf(AddStockStep.PRODUCT_DETAILS) }

    // State for Product Details (Step 1)
    var productName by remember { mutableStateOf("") }
    var productImage: Any? by remember { mutableStateOf(null) }
    var categoryType by remember { mutableStateOf("") }
    var subCategory by remember { mutableStateOf("") }

    // State for Quantity & Expiry (Step 2)
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pcs") }
    var prepMethod by remember { mutableStateOf("") }
    var hasExpiry by remember { mutableStateOf(false) }
    var expiryDates by remember { mutableStateOf(List(0) { "" }) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dropdown menu states
    var categoryExpanded by remember { mutableStateOf(false) }
    val categoryOptions = listOf("Edible", "Non-Edible")

    var subCategoryExpanded by remember { mutableStateOf(false) }
    val edibleSubCategories = listOf("Raw", "Syrup", "Powder", "Liquid", "Dried", "Frozen", "Fresh")
    val nonEdibleSubCategories = listOf("Cups", "Lids", "Straws", "Packaging", "Cleaning Supplies")

    var unitExpanded by remember { mutableStateOf(false) }
    val unitOptions = listOf("Pc", "Kg", "G", "ML", "L")

    var prepMethodExpanded by remember { mutableStateOf(false) }
    val prepMethodOptions = listOf("Needs to be Cooked/Prepped", "Direct Open", "Ready-to-Use")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep != AddStockStep.PRODUCT_DETAILS && currentStep != AddStockStep.SUCCESS) {
                        IconButton(onClick = {
                            currentStep = when (currentStep) {
                                AddStockStep.QUANTITY_EXPIRY -> AddStockStep.PRODUCT_DETAILS
                                AddStockStep.PREVIEW -> AddStockStep.QUANTITY_EXPIRY
                                else -> AddStockStep.PRODUCT_DETAILS
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                    Text(
                        text = "Add A Stock",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    if (currentStep == AddStockStep.PREVIEW) {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        if (productName.isBlank() || quantity.isBlank() || categoryType.isBlank() || unit.isBlank() || prepMethod.isBlank()) {
                                            snackbarHostState.showSnackbar("Please fill all required fields before finishing.")
                                            return@launch
                                        }

                                        val now = Clock.System.now()
                                        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
                                        val dateCreated = "${localDateTime.date} ${localDateTime.time}"

                                        val item = WarehouseItem(
                                            product_name = productName,
                                            product_image_url = productImage?.toString(),
                                            category_type = categoryType,
                                            sub_category = subCategory.ifBlank { null },
                                            quantity = quantity.toDoubleOrNull() ?: 0.0,
                                            unit = unit,
                                            preparation_method = prepMethod,
                                            has_expiry = hasExpiry,
                                            expiry_date = if (hasExpiry) expiryDates.joinToString(";") else null,
                                            date_created = dateCreated
                                        )

                                        repository.insertItem(item)
                                        onItemAdded()
                                        currentStep = AddStockStep.SUCCESS
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Error adding item: ${e.message}")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBEBEBE)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Finish", color = Color.Black)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (currentStep != AddStockStep.SUCCESS) {
                    LinearProgressIndicator(
                        progress = { (currentStep.ordinal + 1) / (AddStockStep.entries.size - 1).toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                when (currentStep) {
                    AddStockStep.PRODUCT_DETAILS -> {
                        Text("Now, Set up your Product name and Product Image also what type of Category", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            label = { Text("Product name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .clickable { /* TODO: Implement image upload */ },
                            contentAlignment = Alignment.Center
                        ) {
                            if (productImage != null) {
                                Text("Image Uploaded", color = Color.Gray)
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Upload here", color = Color.Gray)
                                    Text("(Optional)", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = categoryType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Type of Category") },
                                trailingIcon = {
                                    Icon(Icons.Default.KeyboardArrowDown, "Category Dropdown")
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categoryOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            categoryType = option
                                            categoryExpanded = false
                                            subCategory = ""
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        if (categoryType.isNotBlank()) {
                            val currentSubCategories = when (categoryType) {
                                "Edible" -> edibleSubCategories
                                "Non-Edible" -> nonEdibleSubCategories
                                else -> emptyList()
                            }
                            if (currentSubCategories.isNotEmpty()) {
                                ExposedDropdownMenuBox(
                                    expanded = subCategoryExpanded,
                                    onExpandedChange = { subCategoryExpanded = !subCategoryExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = subCategory,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Category of Product") },
                                        trailingIcon = {
                                            Icon(Icons.Default.KeyboardArrowDown, "Sub-Category Dropdown")
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = subCategoryExpanded,
                                        onDismissRequest = { subCategoryExpanded = false }
                                    ) {
                                        currentSubCategories.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    subCategory = option
                                                    subCategoryExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = {
                                    if (productName.isBlank() || categoryType.isBlank()) {
                                        scope.launch { snackbarHostState.showSnackbar("Product Name and Category are required.") }
                                    } else {
                                        currentStep = AddStockStep.QUANTITY_EXPIRY
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBEBEBE)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Next", color = Color.Black)
                            }
                        }
                    }
                    AddStockStep.QUANTITY_EXPIRY -> {
                        Text("Now, type the Quantity of the product, Preparation Method and set the expiry date", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = {
                                    quantity = it.filter { char -> char.isDigit() }
                                    val q = quantity.toIntOrNull() ?: 0
                                    if (hasExpiry) {
                                        expiryDates = List(q) { index ->
                                            expiryDates.getOrElse(index) { "" }
                                        }
                                    }
                                },
                                label = { Text("Quantity") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(16.dp))

                            ExposedDropdownMenuBox(
                                expanded = unitExpanded,
                                onExpandedChange = { unitExpanded = !unitExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = unit,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Kg, Ml, g...") },
                                    trailingIcon = {
                                        Icon(Icons.Default.KeyboardArrowDown, "Unit Dropdown")
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = unitExpanded,
                                    onDismissRequest = { unitExpanded = false }
                                ) {
                                    unitOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                unit = option
                                                unitExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        ExposedDropdownMenuBox(
                            expanded = prepMethodExpanded,
                            onExpandedChange = { prepMethodExpanded = !prepMethodExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = prepMethod,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Preparation Method") },
                                trailingIcon = {
                                    Icon(Icons.Default.KeyboardArrowDown, "Preparation Method Dropdown")
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = prepMethodExpanded,
                                onDismissRequest = { prepMethodExpanded = false }
                            ) {
                                prepMethodOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            prepMethod = option
                                            prepMethodExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("It has expiry?")
                            Spacer(Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = hasExpiry,
                                    onClick = {
                                        hasExpiry = true
                                        val q = quantity.toIntOrNull() ?: 0
                                        expiryDates = List(q) { "" }
                                    }
                                )
                                Text("Yes")
                            }
                            Spacer(Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = !hasExpiry,
                                    onClick = {
                                        hasExpiry = false
                                        expiryDates = emptyList()
                                    }
                                )
                                Text("No")
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        if (hasExpiry) {
                            val q = quantity.toIntOrNull() ?: 0
                            if (q > 0) {
                                Text("Enter Expiry Dates for each unit:")
                                expiryDates.forEachIndexed { index, date ->
                                    OutlinedTextField(
                                        value = date,
                                        onValueChange = { newValue ->
                                            val newList = expiryDates.toMutableList()
                                            newList[index] = newValue
                                            expiryDates = newList
                                        },
                                        label = { Text("Expiry Date Unit #${index + 1} (YYYY-MM-DD)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                            } else {
                                Text("Please enter a quantity to add expiry dates.")
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = {
                                    if (quantity.isBlank() || unit.isBlank() || prepMethod.isBlank() || (hasExpiry && expiryDates.any { it.isBlank() })) {
                                        scope.launch { snackbarHostState.showSnackbar("Please fill all required fields, including expiry dates if applicable.") }
                                    } else {
                                        currentStep = AddStockStep.PREVIEW
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBEBEBE)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Next", color = Color.Black)
                            }
                        }
                    }
                    AddStockStep.PREVIEW -> {
                        Text("Please review the product and tap finish", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = productName,
                            onValueChange = {},
                            label = { Text("Product name") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = categoryType,
                            onValueChange = {},
                            label = { Text("Type of Category") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = "$quantity $unit",
                            onValueChange = {},
                            label = { Text("Quantity & Measurement") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = if (hasExpiry) expiryDates.joinToString(", ") else "N/A",
                            onValueChange = {},
                            label = { Text("Expiry") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = prepMethod,
                            onValueChange = {},
                            label = { Text("Preparation Method") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (productImage != null) {
                                Text("Uploaded Product Image", color = Color.Gray)
                            } else {
                                Text("No Product Image Uploaded", color = Color.Gray)
                            }
                        }
                    }
                    AddStockStep.SUCCESS -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Success checkmark icon
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(percent = 50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp)
                                )
                            }

                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Adding Successful",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Your Adding stock has been completed successfully.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    onDismiss()
                                    onItemAdded()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                Text("Done", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    )
    SnackbarHost(hostState = snackbarHostState)
}