package com.example.inventory.data.model.repository

import com.example.inventory.data.model.remote.SupabaseService
import com.example.inventory.model.*
import com.example.inventory.model.repository.InventoryRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class InventoryRepositoryImpl(private val supabaseService: SupabaseService) : InventoryRepository {

    override suspend fun createCategory(name: String, description: String?): Long {
        val category = CategoryDTO(id = 0, name = name)
        val result = supabaseService.client.postgrest.from("menu_categories")
            .insert(category) { select() }
            .decodeSingle<CategoryDTO>()
        return result.id
    }

    override suspend fun createProduct(
        categoryId: Long,
        productName: String,
        imageUrl: String?,
        description: String?
    ): Long {
        val product = ProductDTO(
            variantId = 0,
            productId = 0,
            productName = productName,
            variantName = "",
            price = 0.0,
            categoryId = categoryId,
            imageUrl = imageUrl
        )
        val result = supabaseService.client.postgrest.from("menu_products")
            .insert(product) { select() }
            .decodeSingle<ProductDTO>()
        return result.productId
    }

    override suspend fun createVariant(productId: Long, variantName: String, price: Double): Long {
        val variant = VariantDTO(id = 0, product_id = productId, variant_name = variantName, price = price)
        val result = supabaseService.client.postgrest.from("product_variants")
            .insert(variant) { select() }
            .decodeSingle<VariantDTO>()
        return result.id
    }

    override suspend fun createProductRecipe(variantId: Long, ingredients: List<RecipeIngredient>) {
        val recipeIngredients = ingredients.map { it.copy(ingredient_id = variantId) }
        supabaseService.client.postgrest.from("recipe_ingredients").insert(recipeIngredients)
    }

    override suspend fun getProductsForPOS(): List<ProductDTO> {
        val products = supabaseService.client.postgrest.from("products_for_pos")
            .select()
            .decodeList<ProductDTO>()

        val categories = supabaseService.client.postgrest.from("menu_categories")
            .select()
            .decodeList<CategoryDTO>()

        val categoryMap = categories.associateBy({ it.id }, { it.name })
        return products.map { p -> p.copy(categoryName = categoryMap[p.categoryId]) }
    }

    override suspend fun variantHasSufficientStock(variantId: Long): Boolean {
        return supabaseService.client.postgrest
            .rpc("variant_has_sufficient_stock", mapOf("variant_id" to variantId))
            .decodeSingle<Boolean>()
    }

    override suspend fun reserveIngredientsForOrder(orderId: Long, variantId: Long, qty: Int) {
        supabaseService.client.postgrest
            .rpc("reserve_ingredients_for_order", mapOf("order_id" to orderId, "variant_id" to variantId, "qty" to qty))
    }

    override suspend fun finalizeOrderDeduction(orderId: Long) {
        supabaseService.client.postgrest
            .rpc("finalize_order_deduction", mapOf("order_id" to orderId))
    }

    override suspend fun fastTrackRestock(warehouseItemId: Long, transferQuantity: Double, unit: String) {
        supabaseService.client.postgrest
            .rpc("fast_track_restock", mapOf("warehouse_item_id" to warehouseItemId, "transfer_quantity" to transferQuantity, "unit" to unit))
    }
}
