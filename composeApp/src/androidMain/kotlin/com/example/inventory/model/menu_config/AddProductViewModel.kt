package com.example.inventory.model.menu_config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface AddProductViewModel {
    var currentStep: Int
    var useExistingCategory: Boolean
    var newCategoryName: String
    var productName: String
    var productPrice: String
    var productVisibility: Boolean
    var hasVariants: Boolean
    var newVariantName: String
    var newVariantExtraPrice: String
    var variants: List<VariantData>
    var trackStock: Boolean
    var newIngredientName: String
    var newIngredientAmount: String
    var selectedIngredients: List<IngredientData>
    var isDineInAvailable: Boolean
    var isTakeOutAvailable: Boolean
    var productImageUri: String?
    var selectedExistingCategory: Category?

    fun nextStep()
    fun previousStep()
    fun addVariant()
    fun removeVariant(variant: VariantData)
    fun updateVariantPrice(variant: VariantData, value: String)
    fun addIngredient()
    fun removeIngredient(ingredient: IngredientData)
}

class AddProductViewModelImpl : AddProductViewModel {
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
        val newList = variants + VariantData(
            id = "v${variants.size + 1}",
            name = newVariantName,
            extraPrice = newVariantExtraPrice.toDoubleOrNull() ?: 0.0
        )
        variants = newList
    }
    override fun removeVariant(variant: VariantData) {
        variants = variants.filterNot { it == variant }
    }
    override fun updateVariantPrice(variant: VariantData, value: String) {
        variants = variants.map { if (it == variant) it.copy(extraPrice = value.toDoubleOrNull() ?: 0.0) else it }
    }
    override fun addIngredient() {
        val newList = selectedIngredients + IngredientData(
            id = "i${selectedIngredients.size + 1}",
            name = newIngredientName,
            amount = newIngredientAmount.toDoubleOrNull() ?: 0.0
        )
        selectedIngredients = newList
    }
    override fun removeIngredient(ingredient: IngredientData) {
        selectedIngredients = selectedIngredients.filterNot { it == ingredient }
    }
}