package com.example.inventory.data.model.repository

import com.example.inventory.data.model.remote.SupabaseService
import com.example.inventory.model.*
import com.example.inventory.model.repository.InventoryRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import com.example.inventory.model.menu_config.Category
import android.util.Log
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
class InventoryRepositoryImpl(private val supabase: SupabaseService) : InventoryRepository {

    // ✅ FIX: Remove id=0 to let Supabase/Postgres auto-generate it
    override suspend fun createCategory(name: String, description: String?): Long {
        val category = CategoryDTO(
            id = null, // ❗ Let Postgres assign ID automatically
            name = name,
            description = description
        )

        return supabase.client.postgrest
            .from("menu_categories")
            .insert(category) { select() }
            .decodeSingle<CategoryDTO>().id ?: -1L
    }

    // ✅ FIX: Same here, no manual id
    override suspend fun createProduct(
        categoryId: Long,
        productName: String,
        imageUrl: String?,
        description: String?
    ): Long {
        val product = ProductInsertDTO(
            category_id = categoryId,
            product_name = productName,
            image_url = imageUrl,
            description = description
        )

        return supabase.client.postgrest
            .from("menu_products")
            .insert(product) { select() }
            .decodeSingle<ProductInsertResponse>()
            .id
    }

    // ✅ FIX: No id field included, so Supabase handles it
    override suspend fun createVariant(productId: Long, variantName: String, price: Double): Long {
        val variant = VariantInsertDTO(
            product_id = productId,
            variant_name = variantName,
            price = price
        )

        return supabase.client.postgrest
            .from("product_variants")
            .insert(variant) { select() }
            .decodeSingle<VariantDTO>().id
    }

    override suspend fun createProductRecipe(variantId: Long, ingredients: List<RecipeIngredient>) {
        if (ingredients.isNotEmpty()) {
            val payload = ingredients.map { it.copy(variant_id = variantId) }
            supabase.client.postgrest.from("product_recipes").insert(payload)
        }
    }

    suspend fun getWarehouseIngredients(): List<WarehouseItem> {
        return try {
            supabase.client.postgrest
                .from("warehouse_inventory")
                .select()
                .decodeList<WarehouseItem>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getProductsForPOS(): List<ProductDTO> {
        return try {
            supabase.client.postgrest
                .from("products_for_pos")
                .select()
                .decodeList<ProductDTO>()
        } catch (e: Exception) {
            Log.e("InventoryRepo", "Error fetching POS products: ${e.message}")
            emptyList()
        }
    }

    override suspend fun variantHasSufficientStock(variantId: Long): Boolean {
        return try {
            // 1️⃣ Get recipe ingredients for this variant
            val recipeIngredients = supabase.client.postgrest
                .from("product_recipes")
                .select {
                    filter {
                        eq("variant_id", variantId)
                    }
                }
                .decodeList<RecipeIngredient>()

            // If no recipe defined, assume always available
            if (recipeIngredients.isEmpty()) return true

            // 2️⃣ Get all in-kitchen stock
            val inKitchenItems = supabase.client.postgrest
                .from("in_kitchen")
                .select { }
                .decodeList<InKitchenItem>()

            // 3️⃣ Check every ingredient availability
            for (ingredient in recipeIngredients) {
                val available = inKitchenItems
                    .filter { it.warehouse_item_id == ingredient.ingredient_id && it.status == "available" }
                    .sumOf { (it.current_quantity ?: 0.0) - (it.reserved_quantity ?: 0.0) }

                val needed = ingredient.measurement_value ?: 0.0
                if (available < needed) {
                    println("🚫 Not enough stock for ingredient ${ingredient.ingredient_id} (${available}/${needed})")
                    return false
                }
            }

            println("✅ All ingredients available for variant $variantId")
            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun reserveIngredientsForOrder(orderId: Long, variantId: Long, qty: Int) {
        supabase.client.postgrest.rpc(
            "reserve_ingredients_for_order",
            mapOf(
                "order_id" to orderId,
                "variant_id" to variantId,
                "qty" to qty
            )
        )
    }

    override suspend fun finalizeOrderDeduction(orderId: Long) {
        supabase.client.postgrest.rpc("finalize_order_deduction", mapOf("order_id" to orderId))
    }

    suspend fun getMenuCategories(): List<Category> {
        return try {
            supabase.client.postgrest
                .from("menu_categories")
                .select()
                .decodeList<Category>()
        } catch (e: Exception) {
            Log.e("InventoryRepo", "Error loading categories: ${e.message}")
            emptyList()
        }
    }

    override suspend fun fastTrackRestock(warehouseItemId: Long, transferQuantity: Double, unit: String) {
        supabase.client.postgrest.rpc(
            "fast_track_restock",
            mapOf(
                "warehouse_item_id" to warehouseItemId,
                "transfer_quantity" to transferQuantity,
                "unit" to unit
            )
        )
    }
}
