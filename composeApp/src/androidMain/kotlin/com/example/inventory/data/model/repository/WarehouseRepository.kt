package com.example.inventory.data.model.repository

import com.example.inventory.data.model.InKitchenItem
import com.example.inventory.data.model.WarehouseItem
import com.example.inventory.data.model.remote.SupabaseService

class WarehouseRepository(private val supabaseService: SupabaseService) {

    // ✅ Fetch all warehouse items
    suspend fun getAllWarehouseItems(): List<WarehouseItem> {
        return try {
            supabaseService.fetchWarehouseItems()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ✅ Add warehouse item
    suspend fun addWarehouseItem(item: WarehouseItem) {
        try {
            supabaseService.addWarehouseItem(item)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // ✅ Delete warehouse item
    suspend fun deleteWarehouseItem(id: Long): Boolean {
        return try {
            supabaseService.deleteWarehouseItem(id)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ✅ Fetch all in-kitchen items
    suspend fun getAllInKitchenItems(): List<InKitchenItem> {
        return try {
            supabaseService.fetchInKitchenItems()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ✅ Add in-kitchen item
    suspend fun addInKitchenItem(item: InKitchenItem) {
        try {
            supabaseService.addInKitchenItem(item)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // ✅ Transfer a single warehouse item → in_kitchen and remove from warehouse
    suspend fun transferToInKitchenSingle(
        warehouseItem: WarehouseItem,
        transferQuantity: Double,
        unit: String,
        shelfLifeValue: Double?,
        shelfLifeUnit: String?,
        preparationMethod: String,
        expiryIso: String?
    ): Boolean {
        return try {
            supabaseService.transferToInKitchenSingle(
                warehouseItem = warehouseItem,
                transferQuantity = transferQuantity,
                unit = unit,
                shelfLifeValue = shelfLifeValue,
                shelfLifeUnit = shelfLifeUnit,
                preparationMethod = preparationMethod,
                expiryIso = expiryIso
            )
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
