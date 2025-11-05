package com.example.inventory.ui.main.inkitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.model.InKitchenItem
import com.example.inventory.model.WarehouseItem
import com.example.inventory.data.model.repository.WarehouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InKitchenViewModel(
    private val repository: WarehouseRepository
) : ViewModel() {

    private val _inKitchenItems = MutableStateFlow<List<InKitchenItem>>(emptyList())
    val inKitchenItems: StateFlow<List<InKitchenItem>> = _inKitchenItems

    fun loadItems() {
        viewModelScope.launch {
            try {
                // ✅ Correct function name here
                _inKitchenItems.value = repository.fetchInKitchenItems()
            } catch (e: Exception) {
                e.printStackTrace()
                _inKitchenItems.value = emptyList()
            }
        }
    }

    /**
     * Called from PreparationListDialog to transfer one warehouse item → in_kitchen
     * and record it in transfer_history.
     */
    suspend fun transferItemFromWarehouse(
        warehouseItem: WarehouseItem,
        transferQuantity: Double,
        unit: String,
        shelfLifeValue: Double?,
        shelfLifeUnit: String?,
        preparationMethod: String?,
        expiryIso: String?,
        servingSize: Double?,
        useManufacturerExpiry: Boolean
    ): Boolean {
        return try {
            repository.transferToInKitchenSingle(
                warehouseItem = warehouseItem,
                transferQuantity = transferQuantity,
                unit = unit,
                shelfLifeValue = shelfLifeValue,
                shelfLifeUnit = shelfLifeUnit,
                preparationMethod = preparationMethod ?: "Direct Open",
                expiryIso = expiryIso,
                servingSize = servingSize,
                expiryBasedOnManufacturer = useManufacturerExpiry
            )
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
