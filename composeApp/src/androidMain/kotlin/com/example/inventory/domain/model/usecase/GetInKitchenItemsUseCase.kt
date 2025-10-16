package com.example.inventory.domain.model.usecase

import com.example.inventory.data.model.repository.WarehouseRepository

class GetInKitchenItemsUseCase(private val repository: WarehouseRepository) {
    suspend operator fun invoke() = repository.getAllInKitchenItems()
}