package com.example.inventory.data.model.remote

import android.content.Context
import android.net.Uri
import com.example.inventory.data.model.InKitchenItem
import com.example.inventory.data.model.TransferHistory
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
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class SupabaseService(private val client: SupabaseClient) {

    // ✅ Fetch all warehouse items
    suspend fun fetchWarehouseItems(): List<WarehouseItem> = withContext(Dispatchers.IO) {
        client.from("warehouse_inventory").select().decodeList<WarehouseItem>()
    }

    // ✅ Fetch all in-kitchen items
    suspend fun fetchInKitchenItems(): List<InKitchenItem> = withContext(Dispatchers.IO) {
        client.from("in_kitchen").select().decodeList<InKitchenItem>()
    }

    // ✅ Add a new warehouse item
    suspend fun addWarehouseItem(item: WarehouseItem) = withContext(Dispatchers.IO) {
        client.from("warehouse_inventory").insert(item)
    }

    // ✅ Delete warehouse item by ID
    suspend fun deleteWarehouseItem(id: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            client.from("warehouse_inventory").delete {
                filter { eq("id", id) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ✅ Upload image to Supabase Storage
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

    // ✅ Transfer item from warehouse → in_kitchen
    @OptIn(ExperimentalTime::class)
    suspend fun transferToInKitchenSingle(
        warehouseItem: WarehouseItem,
        transferQuantity: Double,
        unit: String,
        shelfLifeValue: Double?,
        shelfLifeUnit: String?,
        preparationMethod: String?,
        expiryIso: String?,
        servingSize: Double?,
        expiryBasedOnManufacturer: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val batchNumber = "BATCH-${Random.nextInt(100000, 999999)}"
            val nowInstant = Clock.System.now()

            // ✅ Calculate expiry date
            val calculatedExpiry = when (shelfLifeUnit?.lowercase()) {
                "hours" -> nowInstant.plus((shelfLifeValue ?: 0.0).toInt().hours)
                "days" -> nowInstant.plus((shelfLifeValue ?: 0.0).toInt().days)
                "weeks" -> nowInstant.plus(((shelfLifeValue ?: 0.0) * 7).toInt().days)
                "months" -> nowInstant.plus(((shelfLifeValue ?: 0.0) * 30).toInt().days)
                else -> null
            }?.toString() ?: expiryIso ?: warehouseItem.expiry_date

            // ✅ Create new in_kitchen record
            val newInKitchenItem = InKitchenItem(
                id = null,
                warehouse_item_id = warehouseItem.id,
                product_name = warehouseItem.product_name,
                product_image_url = warehouseItem.product_image_url,
                category_type = warehouseItem.category_type,
                sub_category = warehouseItem.sub_category,
                batch_number = batchNumber,
                preparation_method = preparationMethod ?: warehouseItem.preparation_method,
                original_quantity = transferQuantity,
                current_quantity = transferQuantity,
                reserved_quantity = 0.0,
                unit = unit,
                serving_size = servingSize,
                shelf_life_value = shelfLifeValue,
                shelf_life_unit = shelfLifeUnit,
                expiry_based_on_manufacturer = expiryBasedOnManufacturer,
                original_expiry_date = warehouseItem.expiry_date,
                calculated_expiry_date = calculatedExpiry,
                status = "available"
            )

            // ✅ Insert into in_kitchen table
            client.from("in_kitchen").insert(newInKitchenItem)

            // ✅ Insert into transfer_history table
            val history = TransferHistory(
                id = null,
                warehouse_item_id = warehouseItem.id,
                product_name = warehouseItem.product_name,
                transfer_quantity = transferQuantity,
                unit = unit,
                preparation_method = preparationMethod ?: warehouseItem.preparation_method,
                shelf_life_value = shelfLifeValue,
                shelf_life_unit = shelfLifeUnit,
                expiry_iso = calculatedExpiry,
                transferred_at = Clock.System.now().toString()
            )
            client.from("transfer_history").insert(history)

            // ✅ Delete the original warehouse item
            warehouseItem.id?.let { safeId ->
                client.from("warehouse_inventory").delete {
                    filter { eq("id", safeId as Any) }
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
