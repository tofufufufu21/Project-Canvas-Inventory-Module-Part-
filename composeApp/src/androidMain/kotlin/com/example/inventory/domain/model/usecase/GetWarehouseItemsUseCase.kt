package com.example.inventory.domain.model.usecase

import com.example.inventory.data.model.repository.WarehouseRepository

class GetWarehouseItemsUseCase(private val repository: WarehouseRepository) {
    suspend operator fun invoke() = repository.getAllWarehouseItems()
}