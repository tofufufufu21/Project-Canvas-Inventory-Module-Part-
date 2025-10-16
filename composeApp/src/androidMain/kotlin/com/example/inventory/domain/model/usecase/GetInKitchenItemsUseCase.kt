package com.example.inventory.domain.model.usecase

import com.example.inventory.data.model.repository.WarehouseRepository
import com.example.inventory.data.model.InKitchenItem

class GetInKitchenItemsUseCase(
    private val repository: WarehouseRepository
) {
    suspend operator fun invoke(): List<InKitchenItem> {
        return repository.fetchInKitchenItems()
    }
}
