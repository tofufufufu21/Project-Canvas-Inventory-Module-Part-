package com.example.inventory

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

class WarehouseRepository(private val supabase: SupabaseClient) {

    // 🔹 Fetch all items from the warehouse_inventory table
    suspend fun getAllItems(): List<WarehouseItem> {
        return try {
            supabase.from("warehouse_inventory")
                .select()
                .decodeList<WarehouseItem>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 🔹 Insert a new item into warehouse_inventory
    suspend fun insertItem(item: WarehouseItem) {
        try {
            supabase.from("warehouse_inventory").insert(item)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // 🔹 Insert a new item into in_kitchen
    suspend fun insertInKitchenItem(item: InKitchenItem): Boolean = withContext(Dispatchers.Default) {
        try {
            supabase.postgrest["in_kitchen"].insert(item)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 🔹 Fetch all items from in_kitchen
    suspend fun getAllInKitchenItems(): List<InKitchenItem> {
        return try {
            supabase.from("in_kitchen")
                .select()
                .decodeList<InKitchenItem>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 🔹 Transfer item from warehouse_inventory to in_kitchen
    @OptIn(ExperimentalTime::class)
    suspend fun transferToInKitchen(
        warehouseItem: WarehouseItem,
        transferQuantity: Double,
        unit: String,
        shelfLifeValue: Double?, // ✅ Use Double to match InKitchenItem
        shelfLifeUnit: String?,
        preparationMethod: String = "Direct Open"
    ): Boolean = withContext(Dispatchers.Default) {
        try {
            val batchNumber = "BATCH-${kotlin.random.Random.nextInt(100000, 999999)}"

            // ✅ Current timestamp
            val currentDate = kotlin.time.Clock.System.now()

            // ✅ Calculate expiry date based on shelf life
            val calculatedExpiry = when (shelfLifeUnit) {
                "days" -> currentDate.plus((shelfLifeValue ?: 0.0).days)
                "weeks" -> currentDate.plus(((shelfLifeValue ?: 0.0) * 7).days)
                "months" -> currentDate.plus(((shelfLifeValue ?: 0.0) * 30).days)
                else -> currentDate
            }.toString()

            // ✅ Prepare item to insert into in_kitchen
            val newInKitchenItem = InKitchenItem(
                warehouse_item_id = warehouseItem.id?.toLong(),
                product_name = warehouseItem.product_name,
                product_image_url = warehouseItem.product_image_url,
                category_type = warehouseItem.category_type,
                sub_category = warehouseItem.sub_category,
                batch_number = batchNumber,
                preparation_method = preparationMethod,
                original_quantity = transferQuantity,
                current_quantity = transferQuantity,
                reserved_quantity = 0.0,
                unit = unit,
                shelf_life_value = shelfLifeValue,
                shelf_life_unit = shelfLifeUnit,
                original_expiry_date = warehouseItem.expiry_date,
                calculated_expiry_date = calculatedExpiry,
                status = "available",
                transferred_at = null
            )

            // ✅ Insert the record into Supabase
            supabase.postgrest["in_kitchen"].insert(newInKitchenItem)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
