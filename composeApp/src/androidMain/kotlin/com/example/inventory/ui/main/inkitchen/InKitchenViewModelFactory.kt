package com.example.inventory.ui.main.inkitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.inventory.data.model.repository.WarehouseRepository

/**
 * ✅ Factory for InKitchenViewModel
 * Matches the new InKitchenViewModel constructor (which uses WarehouseRepository directly)
 */
class InKitchenViewModelFactory(
    private val repository: WarehouseRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InKitchenViewModel(repository) as T
    }
}
