package com.example.inventory.ui.main.warehouse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.WarehouseItem
import com.example.inventory.data.model.remote.SupabaseService
import com.example.inventory.data.model.repository.WarehouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WarehouseViewModel(
    private val repository: WarehouseRepository,
    val supabaseService: SupabaseService
) : ViewModel() {

    private val _warehouseItems = MutableStateFlow<List<WarehouseItem>>(emptyList())
    val warehouseItems: StateFlow<List<WarehouseItem>> = _warehouseItems

    // ✅ Load warehouse items
    fun loadItems() {
        viewModelScope.launch {
            try {
                _warehouseItems.value = repository.getAllWarehouseItems()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✅ Add new warehouse item
    fun addItem(item: WarehouseItem) {
        viewModelScope.launch {
            try {
                repository.addWarehouseItem(item)
                loadItems()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✅ Delete a warehouse item
    fun deleteItem(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteWarehouseItem(id)
                loadItems()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✅ Public wrapper to transfer item → in_kitchen
    suspend fun transferToInKitchen(
        warehouseItem: WarehouseItem,
        transferQuantity: Double,
        unit: String,
        shelfLifeValue: Double?,
        shelfLifeUnit: String?,
        preparationMethod: String,
        expiryIso: String?
    ): Boolean {
        return try {
            repository.transferToInKitchenSingle(
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
