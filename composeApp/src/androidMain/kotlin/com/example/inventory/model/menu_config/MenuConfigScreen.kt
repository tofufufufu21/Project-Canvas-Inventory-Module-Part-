package com.example.inventory.model.menu_config

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// --- MenuConfigScreen and steps ---
// This file expects a real AddProductViewModel in your project for runtime.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuConfigScreen(vm: AddProductViewModel) {
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
            Column(modifier = Modifier.fillMaxSize()) {
                LinearProgressIndicator(
                    progress = { step / 7f }, // Updated to use lambda for progress
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
                HorizontalDivider(Modifier.padding(vertical = 16.dp)) // Changed to HorizontalDivider

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

                NavButtons(vm)
            }
        }
    }
}

@Composable
fun Step1CategorySelection(vm: AddProductViewModel) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Step 1: Category Selection", style = MaterialTheme.typography.titleLarge)
        Text("Choose an existing category or create a new one.", style = MaterialTheme.typography.bodyMedium)

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
    }
}

@Composable
fun Step2AddProduct(vm: AddProductViewModel) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Step 2: Add Product Details", style = MaterialTheme.typography.titleLarge)
        Text("Enter product information and upload an image.", style = MaterialTheme.typography.bodyMedium)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .clickable { /* image picker stub */ },
                contentAlignment = Alignment.Center
            ) { Text("Upload here", color = Color.Gray) }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = vm.productName,
                    onValueChange = { vm.productName = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = vm.productPrice,
                    onValueChange = { vm.productPrice = it },
                    label = { Text("Price") },
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
    }
}

@Composable
fun Step3Variants(vm: AddProductViewModel) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Step 3: Product Variants", style = MaterialTheme.typography.titleLarge)
        Text("Does this product have different sizes, colors, or options?", style = MaterialTheme.typography.bodyMedium)

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
                    label = { Text("Extra Price (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp)
                )
                Button(onClick = { vm.addVariant() }, enabled = vm.newVariantName.isNotBlank()) {
                    Icon(Icons.Default.Add, contentDescription = "Add Variant")
                }
            }

            if (vm.variants.isNotEmpty()) {
                Text("Current Variants:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    items(vm.variants) { variant ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${variant.name} (+₱${variant.extraPrice})")
                            IconButton(onClick = { vm.removeVariant(variant) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Variant")
                            }
                        }
                        HorizontalDivider() // Changed to HorizontalDivider
                    }
                }
            }
        }
    }
}

@Composable
fun Step4StockTracking(vm: AddProductViewModel) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Step 4: Stock Tracking", style = MaterialTheme.typography.titleLarge)
        Text("Do you want to track ingredients for this product?", style = MaterialTheme.typography.bodyMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = vm.trackStock, onClick = { vm.trackStock = true })
            Text("Yes, track ingredients", modifier = Modifier.clickable { vm.trackStock = true }.padding(start = 8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = !vm.trackStock, onClick = { vm.trackStock = false })
            Text("No, don't track stock", modifier = Modifier.clickable { vm.trackStock = false }.padding(start = 8.dp))
        }

        if (vm.trackStock) {
            Text("Allocate Ingredients per Variant:", style = MaterialTheme.typography.titleSmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = vm.newIngredientName,
                    onValueChange = { vm.newIngredientName = it },
                    label = { Text("Ingredient") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = vm.newIngredientAmount,
                    onValueChange = { vm.newIngredientAmount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(100.dp)
                )
                Button(onClick = { vm.addIngredient() }, enabled = vm.newIngredientName.isNotBlank() && vm.newIngredientAmount.isNotBlank()) {
                    Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
                }
            }

            if (vm.selectedIngredients.isNotEmpty()) {
                Text("Allocated Ingredients:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    items(vm.selectedIngredients) { ingredient ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${ingredient.name}: ${ingredient.amount}")
                            IconButton(onClick = { vm.removeIngredient(ingredient) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Ingredient")
                            }
                        }
                        HorizontalDivider() // Changed to HorizontalDivider
                    }
                }
            }
        }
    }
}

@Composable
fun Step5PricingAvailability(vm: AddProductViewModel) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Step 5: Pricing & Availability", style = MaterialTheme.typography.titleLarge)
        Text("Set prices and availability for each variant.", style = MaterialTheme.typography.bodyMedium)

        if (vm.variants.isEmpty()) {
            Text("No variants added. Showing base product pricing.", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = vm.productPrice,
                onValueChange = { vm.productPrice = it },
                label = { Text("Base Price (₱)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text("Variant Pricing:", style = MaterialTheme.typography.titleSmall)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                items(vm.variants) { variant ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(variant.name, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = variant.extraPrice.toString(),
                            onValueChange = { vm.updateVariantPrice(variant, it) },
                            label = { Text("Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(120.dp)
                        )
                    }
                    HorizontalDivider() // Changed to HorizontalDivider
                }
            }
        }

        Text("Availability:", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = vm.isDineInAvailable, onCheckedChange = { vm.isDineInAvailable = it })
            Text("Available for Dine-In", modifier = Modifier.padding(start = 8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = vm.isTakeOutAvailable, onCheckedChange = { vm.isTakeOutAvailable = it })
            Text("Available for Take-Out", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun Step6Review(vm: AddProductViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp
        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) { ReviewSummary(vm) }
                Column(modifier = Modifier.weight(1f)) { ImagePreview(vm.productImageUri) }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                ReviewSummary(vm)
                Spacer(Modifier.height(16.dp))
                ImagePreview(vm.productImageUri)
            }
        }
    }
}

@Composable
private fun ReviewSummary(vm: AddProductViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Step 6: Review Product", style = MaterialTheme.typography.titleLarge)
        Text("Please review the details before confirming.", style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) // Changed to HorizontalDivider

        Text("Category:", fontWeight = FontWeight.Bold)
        Text(if (vm.useExistingCategory) vm.selectedExistingCategory?.name ?: "N/A" else vm.newCategoryName)

        Text("Product Name:", fontWeight = FontWeight.Bold)
        Text(vm.productName)

        Text("Base Price:", fontWeight = FontWeight.Bold)
        Text("₱${vm.productPrice}")

        Text("Visibility:", fontWeight = FontWeight.Bold)
        Text(if (vm.productVisibility) "Visible" else "Hidden")

        if (vm.hasVariants) {
            Text("Variants:", fontWeight = FontWeight.Bold)
            vm.variants.forEach { variant -> Text("- ${variant.name} (+₱${variant.extraPrice})") }
        }

        if (vm.trackStock) {
            Text("Stock Tracking:", fontWeight = FontWeight.Bold)
            vm.selectedIngredients.forEach { ingredient -> Text("- ${ingredient.name}: ${ingredient.amount}") }
        }

        Text("Dine-In Available:", fontWeight = FontWeight.Bold)
        Text(vm.isDineInAvailable.toString())

        Text("Take-Out Available:", fontWeight = FontWeight.Bold)
        Text(vm.isTakeOutAvailable.toString())
    }
}

@Composable
private fun ImagePreview(imageUri: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) Text("Image: $imageUri") else Text("No Image Uploaded", color = Color.Gray)
    }
}

@Composable
fun Step7Confirmation() { // Removed vm parameter
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
            text = "Your first product is now ready to sell!",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun NavButtons(vm: AddProductViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = { vm.previousStep() }, enabled = vm.currentStep > 1) { Text("Back") }
        Button(onClick = { vm.nextStep() }, enabled = vm.currentStep < 7) { Text(if (vm.currentStep == 6) "Confirm" else "Next") }
    }
}
