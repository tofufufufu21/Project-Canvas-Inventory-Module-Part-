package com.example.inventory.domain.model.usecase

import com.example.inventory.model.WarehouseItem
import com.example.inventory.data.model.repository.WarehouseRepository

class AddStockUseCase(private val repository: WarehouseRepository) {
    suspend operator fun invoke(item: WarehouseItem) = repository.addWarehouseItem(item)
}