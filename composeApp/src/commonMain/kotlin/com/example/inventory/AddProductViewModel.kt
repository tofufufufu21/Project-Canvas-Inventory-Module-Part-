package com.example.inventory

data class VariantData(val id: String, val name: String, val extraPrice: Double)
data class IngredientData(val id: String, val name: String, val amount: Double)

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

data class Category(val name: String)
