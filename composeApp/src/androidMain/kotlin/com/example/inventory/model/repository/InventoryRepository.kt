package com.example.inventory.model.repository

import com.example.inventory.model.RecipeIngredient
import com.example.inventory.model.ProductDTO

/**
 * Shared interface for inventory / menu / POS operations.
 *
 * Platform-specific implementations (e.g., androidMain using Supabase) should implement
 * this interface and provide the actual network / database behavior.
 */
interface InventoryRepository {

    // ---------- Menu configuration ----------

    /**
     * Create a new menu category.
     *
     * @param name category name
     * @param description optional description
     * @return the created category id (database id) as Long
     */
    suspend fun createCategory(name: String, description: String? = null): Long

    /**
     * Create a product record linked to the given category.
     *
     * @param categoryId id of category this product belongs to
     * @param productName product display name
     * @param imageUrl optional image URL
     * @param description optional product description
     * @return the created product id as Long
     */
    suspend fun createProduct(
        categoryId: Long,
        productName: String,
        imageUrl: String?,
        description: String?
    ): Long

    /**
     * Create a variant for the specified product.
     *
     * @param productId parent product id
     * @param variantName display name of variant
     * @param price price or extra price (implementation detail)
     * @return the created variant id as Long
     */
    suspend fun createVariant(productId: Long, variantName: String, price: Double): Long

    /**
     * Attach recipe (ingredients) to a variant.
     *
     * The implementation should persist the ingredient list for the given variant.
     *
     * @param variantId target variant id
     * @param ingredients list of RecipeIngredient to attach
     */
    suspend fun createProductRecipe(variantId: Long, ingredients: List<RecipeIngredient>)

    // ---------- Point Of Sale (POS) ----------

    /**
     * Get products and necessary details for the POS screen.
     *
     * @return list of ProductDTO used by the POS UI
     */
    suspend fun getProductsForPOS(): List<ProductDTO>

    /**
     * Check whether a variant currently has sufficient stock to fulfill at least one unit.
     *
     * @param variantId variant id to check
     * @return true if stock is sufficient, false otherwise
     */
    suspend fun variantHasSufficientStock(variantId: Long): Boolean

    /**
     * Reserve ingredients when an order is placed (mark them as pending/reserved).
     *
     * @param orderId id of the order (local or server id)
     * @param variantId variant being ordered
     * @param qty quantity ordered
     */
    suspend fun reserveIngredientsForOrder(orderId: Long, variantId: Long, qty: Int)

    /**
     * Finalize an order and deduct reserved ingredients from stock.
     *
     * @param orderId id of the order to finalize
     */
    suspend fun finalizeOrderDeduction(orderId: Long)

    /**
     * Fast-track restock (e.g., create a transfer request from warehouse to kitchen).
     *
     * @param warehouseItemId id of the warehouse item to restock
     * @param transferQuantity quantity to transfer
     * @param unit unit of measurement for the quantity (e.g., "kg", "g", "pcs")
     */
    suspend fun fastTrackRestock(warehouseItemId: Long, transferQuantity: Double, unit: String)
}
