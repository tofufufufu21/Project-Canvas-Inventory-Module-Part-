package com.example.inventory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.inventory.pos.PosMainScreen
import com.example.inventory.pos.PosViewModel
// Removed: import com.example.inventory.Category // Added: Import the Category data class

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(posViewModel: PosViewModel, inventoryScreen: @Composable () -> Unit) {
    var selected by remember { mutableStateOf(0) }
    val items = listOf("Dashboard", "Analytics", "Inventory", "Menu Config")
    val icons: List<ImageVector> = listOf(
        Icons.Default.Dashboard, 
        Icons.Default.Analytics, 
        Icons.Default.Inventory, 
        Icons.AutoMirrored.Filled.MenuBook
    )

    val dummyAddProductViewModel = remember {
        object : AddProductViewModel {
            override var currentStep: Int by mutableStateOf(1)
            override var useExistingCategory: Boolean by mutableStateOf(false)
            override var newCategoryName: String by mutableStateOf("")
            override var productName: String by mutableStateOf("")
            override var productPrice: String by mutableStateOf("")
            override var productVisibility: Boolean by mutableStateOf(true)
            override var hasVariants: Boolean by mutableStateOf(false)
            override var newVariantName: String by mutableStateOf("")
            override var newVariantExtraPrice: String by mutableStateOf("")
            override var variants: List<VariantData> by mutableStateOf(emptyList())
            override var trackStock: Boolean by mutableStateOf(false)
            override var newIngredientName: String by mutableStateOf("")
            override var newIngredientAmount: String by mutableStateOf("")
            override var selectedIngredients: List<IngredientData> by mutableStateOf(emptyList())
            override var isDineInAvailable: Boolean by mutableStateOf(false)
            override var isTakeOutAvailable: Boolean by mutableStateOf(false)
            override var productImageUri: String? by mutableStateOf(null)
            override var selectedExistingCategory: Category? by mutableStateOf(null)

            override fun nextStep() { currentStep += 1 }
            override fun previousStep() { if (currentStep > 0) currentStep -= 1 }
            override fun addVariant() {
                val newList = variants + VariantData(id = "v${variants.size + 1}", name = newVariantName, extraPrice = newVariantExtraPrice.toDoubleOrNull() ?: 0.0)
                variants = newList
            }
            override fun removeVariant(variant: VariantData) {
                variants = variants.filterNot { it == variant }
            }
            override fun updateVariantPrice(variant: VariantData, value: String) {
                variants = variants.map { if (it == variant) it.copy(extraPrice = value.toDoubleOrNull() ?: 0.0) else it }
            }
            override fun addIngredient() {
                val newList = selectedIngredients + IngredientData(id = "i${selectedIngredients.size + 1}", name = newIngredientName, amount = newIngredientAmount.toDoubleOrNull() ?: 0.0)
                selectedIngredients = newList
            }
            override fun removeIngredient(ingredient: IngredientData) {
                selectedIngredients = selectedIngredients.filterNot { it == ingredient }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* No title */ },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Machine Logo",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Handle notifications */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                    IconButton(onClick = { /* TODO: Handle settings */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Row(Modifier.fillMaxSize().padding(innerPadding)) {
            NavigationRail {
                Spacer(Modifier.weight(1f))
                items.forEachIndexed { index, item ->
                    NavigationRailItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selected == index,
                        onClick = { selected = index }
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Box(Modifier.weight(1f)) {
                when (selected) {
                    0 -> PosMainScreen(posViewModel)
                    1 -> AnalyticsScreenContent()
                    2 -> inventoryScreen()
                    3 -> MenuConfigScreen(dummyAddProductViewModel)
                }
            }
        }
    }
}