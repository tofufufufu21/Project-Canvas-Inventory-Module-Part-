package com.example.inventory.ui.main.inkitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.InKitchenItem
import com.example.inventory.data.model.WarehouseItem
import com.example.inventory.data.model.repository.WarehouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InKitchenViewModel(private val repository: WarehouseRepository) : ViewModel() {

    private val _inKitchenItems = MutableStateFlow<List<InKitchenItem>>(emptyList())
    val inKitchenItems: StateFlow<List<InKitchenItem>> = _inKitchenItems

    init {
        loadItems()
    }

    // ✅ Load all in-kitchen items
    fun loadItems() {
        viewModelScope.launch {
            val list = repository.getAllInKitchenItems()
            _inKitchenItems.value = list
        }
    }

    // ✅ Transfer one item from warehouse → in_kitchen (single row)
    fun transferFromWarehouseSingle(
        warehouseItem: WarehouseItem,
        transferQuantity: Double,
        unit: String,
        shelfLifeValue: Double?,
        shelfLifeUnit: String?,
        preparationMethod: String,
        expiryIso: String? = null
    ) {
        viewModelScope.launch {
            repository.transferToInKitchenSingle(
                warehouseItem,
                transferQuantity,
                unit,
                shelfLifeValue,
                shelfLifeUnit,
                preparationMethod,
                expiryIso
            )
            loadItems()
        }
    }
}
