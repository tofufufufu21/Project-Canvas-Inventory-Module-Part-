package com.example.inventory.data.model.repository

import android.content.Context
import android.net.Uri
import com.example.inventory.data.model.InKitchenItem
import com.example.inventory.data.model.WarehouseItem
import com.example.inventory.data.model.remote.SupabaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WarehouseRepository(
    private val supabaseService: SupabaseService
) {

    // ✅ Fetch all warehouse items
    suspend fun fetchWarehouseItems(): List<WarehouseItem> = withContext(Dispatchers.IO) {
        supabaseService.fetchWarehouseItems()
    }

    // ✅ Fetch all in-kitchen items
    suspend fun fetchInKitchenItems(): List<InKitchenItem> = withContext(Dispatchers.IO) {
        supabaseService.fetchInKitchenItems()
    }

    // ✅ Add new warehouse item
    suspend fun addWarehouseItem(item: WarehouseItem) = withContext(Dispatchers.IO) {
        supabaseService.addWarehouseItem(item)
    }

    // ✅ Delete warehouse item by ID
    suspend fun deleteWarehouseItem(id: Long): Boolean = withContext(Dispatchers.IO) {
        supabaseService.deleteWarehouseItem(id)
    }

    // ✅ Upload image to Supabase Storage
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        bucket: String = "product-images"
    ): String? = withContext(Dispatchers.IO) {
        supabaseService.uploadImage(context, imageUri, bucket)
    }

    // ✅ Transfer item from warehouse → in_kitchen
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
        supabaseService.transferToInKitchenSingle(
            warehouseItem,
            transferQuantity,
            unit,
            shelfLifeValue,
            shelfLifeUnit,
            preparationMethod,
            expiryIso,
            servingSize,
            expiryBasedOnManufacturer
        )
    }
}
