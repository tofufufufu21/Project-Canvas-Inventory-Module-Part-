package com.example.inventory.data.model.repository

import android.content.Context
import android.net.Uri
import com.example.inventory.data.model.InKitchenItem
import com.example.inventory.data.model.WarehouseItem
import com.example.inventory.data.model.remote.SupabaseService
import com.example.inventory.pos.Category
import com.example.inventory.pos.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// This class implements the ProductsRepository interface for the PosViewModel.
class WarehouseRepository(
    private val supabaseService: SupabaseService
) : ProductsRepository {

    // This function fulfills the contract from the commonMain ProductsRepository interface
    override suspend fun getProductsForPOS(): List<Product> = withContext(Dispatchers.IO) {
        try {
            supabaseService.fetchWarehouseItems().map { warehouseItem ->
                Product(
                    id = warehouseItem.id.toString(),
                    name = warehouseItem.product_name,
                    price = 0.0, // IMPORTANT: Add a 'price' column to your Supabase table
                    category = try { Category.valueOf(warehouseItem.category_type) } catch (e: Exception) { Category.Snacks },
                    stock = warehouseItem.quantity.toInt()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // This function is for the Inventory screen (and other internal uses)
    suspend fun fetchWarehouseItems(): List<WarehouseItem> = withContext(Dispatchers.IO) {
        supabaseService.fetchWarehouseItems()
    }

    // These are other functions specific to the Android implementation
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