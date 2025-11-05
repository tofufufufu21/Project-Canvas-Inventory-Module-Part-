package com.example.inventory.data.model.repository

import android.content.Context
import android.net.Uri
import com.example.inventory.data.model.remote.SupabaseService
import com.example.inventory.model.InKitchenItem
import com.example.inventory.model.WarehouseItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WarehouseRepository
 * Handles warehouse and in-kitchen inventory operations.
 */
class WarehouseRepository(private val supabaseService: SupabaseService) {

    suspend fun fetchWarehouseItems(): List<WarehouseItem> = withContext(Dispatchers.IO) {
        supabaseService.fetchWarehouseItems()
    }

    suspend fun fetchInKitchenItems(): List<InKitchenItem> = withContext(Dispatchers.IO) {
        supabaseService.fetchInKitchenItems()
    }

    suspend fun addWarehouseItem(item: WarehouseItem) = withContext(Dispatchers.IO) {
        supabaseService.addWarehouseItem(item)
    }

    suspend fun deleteWarehouseItem(id: Long): Boolean = withContext(Dispatchers.IO) {
        supabaseService.deleteWarehouseItem(id)
    }

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        bucket: String = "product-images"
    ): String? = withContext(Dispatchers.IO) {
        supabaseService.uploadImage(context, imageUri, bucket)
    }

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
