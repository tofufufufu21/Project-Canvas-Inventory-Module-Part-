package com.example.inventory.domain.model.usecase

import com.example.inventory.data.model.repository.WarehouseRepository
import com.example.inventory.data.model.WarehouseItem

class GetWarehouseItemsUseCase(
    private val repository: WarehouseRepository
) {
    suspend operator fun invoke(): List<WarehouseItem> {
        return repository.fetchWarehouseItems()
    }
}
