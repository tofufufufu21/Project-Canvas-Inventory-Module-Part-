package com.example.inventory.ui.main.warehouse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.inventory.data.model.remote.SupabaseService
import com.example.inventory.data.model.repository.WarehouseRepository

class WarehouseViewModelFactory(
    private val repository: WarehouseRepository,
    private val supabaseService: SupabaseService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WarehouseViewModel(repository, supabaseService) as T
    }
}
