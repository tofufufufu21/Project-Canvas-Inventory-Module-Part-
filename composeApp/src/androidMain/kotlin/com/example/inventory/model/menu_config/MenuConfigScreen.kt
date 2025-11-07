package com.example.inventory.model.menu_config

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.inventory.model.WarehouseItem
import com.example.inventory.pos.PosViewModel
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

import io.github.jan.supabase.postgrest.postgrest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuConfigScreen(
    vm: AddProductViewModel,
    posViewModel: PosViewModel? = null
) {
    val scope = rememberCoroutineScope()
    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    ) {
        val isWideScreen = maxWidth > 600.dp
        val step = vm.currentStep

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(if (isWideScreen) 0.8f else 0.95f)
                .fillMaxHeight(if (isWideScreen) 0.9f else 0.95f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                LinearProgressIndicator(
                    progress = { step / 7f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF3D5AFE)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Add New Product",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    text = "Step $step of 7",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        (fadeIn(tween(400, easing = FastOutSlowInEasing)) +
                                scaleIn(initialScale = 0.9f, animationSpec = tween(400))) togetherWith
                                (fadeOut(tween(400, easing = FastOutSlowInEasing)) +
                                        scaleOut(targetScale = 0.9f, animationSpec = tween(400)))
                    },
                    label = "WizardStepAnimation",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) { targetStep ->
                    when (targetStep) {
                        1 -> Step1CategorySelection(vm)
                        2 -> Step2AddProduct(vm)
                        3 -> Step3Variants(vm)
                        4 -> Step4StockTracking(vm)
                        5 -> Step5PricingAvailability(vm)
                        6 -> Step6Review(vm)
                        7 -> Step7Confirmation()
                    }
                }

                NavButtons(
                    vm = vm,
                    isSaving = isSaving,
                    onSave = {
                        scope.launch {
                            isSaving = true
                            val result = vm.saveProduct()
                            isSaving = false
                            if (result.isSuccess) {
                                showSuccess = true
                                posViewModel?.loadProducts()
                                vm.nextStep()
                            } else {
                                showError = result.exceptionOrNull()?.message ?: "Unknown error"
                            }
                        }
                    }
                )
            }
        }

        if (showSuccess) {
            AlertDialog(
                onDismissRequest = { showSuccess = false },
                title = { Text("✅ Success") },
                text = { Text("Product saved successfully to Supabase.") },
                confirmButton = {
                    TextButton(onClick = { showSuccess = false }) {
                        Text("OK")
                    }
                }
            )
        }

        showError?.let { err ->
            AlertDialog(
                onDismissRequest = { showError = null },
                title = { Text("❌ Error") },
                text = { Text(err) },
                confirmButton = {
                    TextButton(onClick = { showError = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

// -------------------- STEP 1 --------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1CategorySelection(vm: AddProductViewModel) {
    val scope = rememberCoroutineScope()

    // Automatically load existing categories when screen opens
    LaunchedEffect(Unit) {
        scope.launch { vm.loadExistingCategories() }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Step 1: Category Selection", style = MaterialTheme.typography.titleLarge)

        // Option 1 – Create a new category
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = !vm.useExistingCategory,
                onClick = { vm.useExistingCategory = false }
            )
            Text(
                "Create a new Category",
                modifier = Modifier
                    .clickable { vm.useExistingCategory = false }
                    .padding(start = 8.dp)
            )
        }

        if (!vm.useExistingCategory) {
            OutlinedTextField(
                value = vm.newCategoryName,
                onValueChange = { vm.newCategoryName = it },
                label = { Text("Category name") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Option 2 – Use existing category
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = vm.useExistingCategory,
                onClick = { vm.useExistingCategory = true }
            )
            Text(
                "Use Existing Category",
                modifier = Modifier
                    .clickable { vm.useExistingCategory = true }
                    .padding(start = 8.dp)
            )
        }

        // ✅ Always show dropdown when "Use Existing" is selected
        if (vm.useExistingCategory) {
            if (vm.existingCategories.isEmpty()) {
                Text("Loading categories or none found...", color = Color.Gray)
            } else {
                var expanded by remember { mutableStateOf(false) }
                var selectedName by remember {
                    mutableStateOf(vm.selectedExistingCategory?.name ?: "")
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedName.ifBlank { "Select Category" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Category") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        vm.existingCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    vm.selectedExistingCategory = category
                                    selectedName = category.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


// -------------------- STEP 2 --------------------
@Composable
fun Step2AddProduct(vm: AddProductViewModel) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Step 2: Add Product Details", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = vm.productName,
            onValueChange = { vm.productName = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = vm.productPrice,
            onValueChange = { vm.productPrice = it },
            label = { Text("Price (₱)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Visible to customers")
            Spacer(Modifier.width(8.dp))
            Switch(checked = vm.productVisibility, onCheckedChange = { vm.productVisibility = it })
        }
    }
}

// -------------------- STEP 3 --------------------
@Composable
fun Step3Variants(vm: AddProductViewModel) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Step 3: Product Variants", style = MaterialTheme.typography.titleLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = vm.hasVariants, onClick = { vm.hasVariants = true })
            Text("Yes, add variants", modifier = Modifier.clickable { vm.hasVariants = true }.padding(start = 8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = !vm.hasVariants, onClick = { vm.hasVariants = false })
            Text("No variants", modifier = Modifier.clickable { vm.hasVariants = false }.padding(start = 8.dp))
        }

        if (vm.hasVariants) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = vm.newVariantName,
                    onValueChange = { vm.newVariantName = it },
                    label = { Text("Variant Name") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = vm.newVariantExtraPrice,
                    onValueChange = { vm.newVariantExtraPrice = it },
                    label = { Text("Extra Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp)
                )
                Button(onClick = { vm.addVariant() }, enabled = vm.newVariantName.isNotBlank()) {
                    Icon(Icons.Default.Add, contentDescription = "Add Variant")
                }
            }

            if (vm.variants.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                    items(vm.variants) { variant ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${variant.name} (+₱${variant.extraPrice})")
                            IconButton(onClick = { vm.removeVariant(variant) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------- STEP 4 --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step4StockTracking(vm: AddProductViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Automatically load ingredients from warehouse
    LaunchedEffect(Unit) {
        vm.loadWarehouseIngredients()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Step 4: Stock Tracking", style = MaterialTheme.typography.titleLarge)
        Text("Link this menu item to ingredients from your warehouse inventory.", style = MaterialTheme.typography.bodyMedium)

        // --- Enable or Disable Stock Tracking ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = vm.trackStock, onClick = { vm.trackStock = true })
            Text(
                "Yes, track ingredients",
                modifier = Modifier.clickable { vm.trackStock = true }.padding(start = 8.dp)
            )
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = !vm.trackStock, onClick = { vm.trackStock = false })
            Text(
                "No, don't track stock",
                modifier = Modifier.clickable { vm.trackStock = false }.padding(start = 8.dp)
            )
        }

        // --- Ingredient Linking Section ---
        if (vm.trackStock) {
            var selectedIngredient by remember { mutableStateOf<WarehouseItem?>(null) }
            var quantity by remember { mutableStateOf("") }
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedIngredient?.product_name ?: "",
                    onValueChange = {},
                    label = { Text("Select Ingredient") },
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .clickable { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    if (vm.warehouseItems.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No ingredients available in warehouse") },
                            onClick = { expanded = false }
                        )
                    } else {
                        vm.warehouseItems.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${item.product_name} — ${item.quantity}${item.unit ?: ""}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedIngredient = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (selectedIngredient != null && quantity.isNotBlank()) {
                        val amount = quantity.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            // Prevent duplicates
                            if (vm.selectedIngredients.none { it.ingredient_id == selectedIngredient!!.id }) {
                                vm.addIngredient(selectedIngredient!!, amount)
                                quantity = ""
                                Toast.makeText(context, "Ingredient added", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Ingredient already added!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Please select an ingredient", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
                Spacer(Modifier.width(8.dp))
                Text("Add Ingredient")
            }

            // --- Display Selected Ingredients ---
            Spacer(Modifier.height(12.dp))
            Text("Selected Ingredients:", style = MaterialTheme.typography.titleMedium)

            if (vm.selectedIngredients.isEmpty()) {
                Text("No ingredients added yet.", color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    items(vm.selectedIngredients) { ing ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "- ID: ${ing.ingredient_id}, ${ing.measurement_value}${ing.measurement_unit}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(onClick = {
                                vm.selectedIngredients =
                                    vm.selectedIngredients.filterNot { it.ingredient_id == ing.ingredient_id }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Ingredient")
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------- STEP 5 --------------------
@Composable
fun Step5PricingAvailability(vm: AddProductViewModel) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Step 5: Pricing & Availability", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = vm.productPrice,
            onValueChange = { vm.productPrice = it },
            label = { Text("Base Price (₱)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = vm.isDineInAvailable, onCheckedChange = { vm.isDineInAvailable = it })
            Text("Available for Dine-In")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = vm.isTakeOutAvailable, onCheckedChange = { vm.isTakeOutAvailable = it })
            Text("Available for Take-Out")
        }
    }
}

// -------------------- STEP 6 --------------------
@Composable
fun Step6Review(vm: AddProductViewModel) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Step 6: Review Product", style = MaterialTheme.typography.titleLarge)
        Text("Category: ${if (vm.useExistingCategory) vm.selectedExistingCategory?.name ?: "N/A" else vm.newCategoryName}")
        Text("Product: ${vm.productName}")
        Text("Base Price: ₱${vm.productPrice}")
        Text("Variants: ${if (vm.hasVariants) vm.variants.joinToString { it.name } else "None"}")
        Text("Track Stock: ${vm.trackStock}")
        Text("Dine-In: ${vm.isDineInAvailable}, Take-Out: ${vm.isTakeOutAvailable}")
    }
}

// -------------------- STEP 7 --------------------
@Composable
fun Step7Confirmation() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF2D6A4F),
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Your product is now ready to sell!",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
    }
}

// -------------------- NAV BUTTONS --------------------
@Composable
fun NavButtons(
    vm: AddProductViewModel,
    isSaving: Boolean = false,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = { vm.previousStep() }, enabled = vm.currentStep > 1 && !isSaving) {
            Text("Back")
        }

        if (vm.currentStep == 6) {
            Button(onClick = onSave, enabled = !isSaving) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (isSaving) "Saving..." else "Save Product")
            }
        } else {
            Button(onClick = { vm.nextStep() }, enabled = !isSaving) {
                Text(if (vm.currentStep == 7) "Finish" else "Next")
            }
        }
    }
}
