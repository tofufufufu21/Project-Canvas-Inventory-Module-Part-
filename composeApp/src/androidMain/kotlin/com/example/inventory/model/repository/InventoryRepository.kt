package com.example.inventory.model.repository

// commonMain/src/commonMain/kotlin/com/example/inventory/repository/InventoryRepository.kt

import com.example.inventory.model.RecipeIngredient
import com.example.inventory.model.ProductDTO

/**
 * Shared interface for inventory/menu/pos operations used by commonMain viewmodels.
 * Implement this in androidMain with SupabaseClient (platform implementation).
 */
interface InventoryRepository {
    // Menu config
    suspend fun createCategory(name: String, description: String? = null): Long
    suspend fun createProduct(categoryId: Long, productName: String, imageUrl: String?, description: String?): Long
    suspend fun createVariant(productId: Long, variantName: String, price: Double): Long
    suspend fun createProductRecipe(variantId: Long, ingredients: List<RecipeIngredient>)

    // POS
    suspend fun getProductsForPOS(): List<ProductDTO>
    suspend fun variantHasSufficientStock(variantId: Long): Boolean
    suspend fun reserveIngredientsForOrder(orderId: Long, variantId: Long, qty: Int)
    suspend fun finalizeOrderDeduction(orderId: Long)
    suspend fun fastTrackRestock(warehouseItemId: Long, transferQuantity: Double, unit: String)
}
