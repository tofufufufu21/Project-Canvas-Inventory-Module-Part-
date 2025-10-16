package com.example.inventory.data.model.remote

import android.content.Context
import android.net.Uri
import com.example.inventory.data.model.InKitchenItem
import com.example.inventory.data.model.WarehouseItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

class SupabaseService(private val client: SupabaseClient) {

    // ✅ Fetch all warehouse items
    suspend fun fetchWarehouseItems(): List<WarehouseItem> = withContext(Dispatchers.IO) {
        client.from("warehouse_inventory").select().decodeList<WarehouseItem>()
    }

    // ✅ Add warehouse item
    suspend fun addWarehouseItem(item: WarehouseItem) = withContext(Dispatchers.IO) {
        client.from("warehouse_inventory").insert(item)
    }

    // ✅ Delete warehouse item
    suspend fun deleteWarehouseItem(id: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            client.from("warehouse_inventory").delete {
                filter {
                    eq("id", id)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ✅ Fetch in-kitchen items
    suspend fun fetchInKitchenItems(): List<InKitchenItem> = withContext(Dispatchers.IO) {
        client.from("in_kitchen").select().decodeList<InKitchenItem>()
    }

    // ✅ Add in-kitchen item
    suspend fun addInKitchenItem(item: InKitchenItem) = withContext(Dispatchers.IO) {
        client.from("in_kitchen").insert(item)
    }

    // ✅ Transfer warehouse → in_kitchen
    @OptIn(ExperimentalTime::class)
    suspend fun transferToInKitchenSingle(
        warehouseItem: WarehouseItem,
        transferQuantity: Double,
        unit: String,
        shelfLifeValue: Double?,
        shelfLifeUnit: String?,
        preparationMethod: String,
        expiryIso: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Generate unique batch number
            val batchNumber = "BATCH-${Random.nextInt(100000, 999999)}"
            val now = Clock.System.now()

            // Compute expiry
            val expiry = when (shelfLifeUnit) {
                "days" -> now.plus((shelfLifeValue ?: 0.0).days)
                "weeks" -> now.plus(((shelfLifeValue ?: 0.0) * 7).days)
                "months" -> now.plus(((shelfLifeValue ?: 0.0) * 30).days)
                else -> now
            }.toString()

            // Create In-Kitchen item
            val newItem = InKitchenItem(
                warehouse_item_id = warehouseItem.id,
                product_name = warehouseItem.product_name,
                product_image_url = warehouseItem.product_image_url,
                category_type = warehouseItem.category_type,
                sub_category = warehouseItem.sub_category,
                batch_number = batchNumber,
                preparation_method = preparationMethod,
                original_quantity = transferQuantity,
                current_quantity = transferQuantity,
                unit = unit,
                shelf_life_value = shelfLifeValue,
                shelf_life_unit = shelfLifeUnit,
                original_expiry_date = warehouseItem.expiry_date,
                calculated_expiry_date = expiry,
                status = "available"
            )

            // Insert in in_kitchen
            client.from("in_kitchen").insert(newItem)

            // ✅ Delete transferred item from warehouse (fixed type issue)
            client.from("warehouse_inventory").delete {
                filter {
                    eq("id", warehouseItem.id ?: 0)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ✅ Upload image to Supabase Storage (finalized)
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        bucket: String = "product-images"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val bucketRef = client.storage.from(bucket)
            val fileName = "uploads/${System.currentTimeMillis()}.jpg"

            val byteArray = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: return@withContext null

            bucketRef.upload(path = fileName, data = byteArray) {
                upsert = true
                contentType = ContentType.Image.JPEG
            }

            bucketRef.publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
