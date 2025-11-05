package com.example.inventory.model.menu_config

import com.example.inventory.data.model.repository.InventoryRepositoryImpl
import com.example.inventory.model.RecipeIngredient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MenuConfigViewModel(private val repo: InventoryRepositoryImpl) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    fun createFullProductFlow(
        categoryName: String,
        productName: String,
        imageUrl: String?,
        description: String?,
        variants: List<Pair<String, Double>>,
        recipes: Map<String, List<RecipeIngredient>>
    ) {
        scope.launch {
            val catId = repo.createCategory(categoryName)
            val prodId = repo.createProduct(catId, productName, imageUrl, description)
            variants.forEach { (variantName, price) ->
                val variantId = repo.createVariant(prodId, variantName, price)
                val ing = recipes[variantName] ?: emptyList()
                if (ing.isNotEmpty()) repo.createProductRecipe(variantId, ing)
            }
        }
    }
}