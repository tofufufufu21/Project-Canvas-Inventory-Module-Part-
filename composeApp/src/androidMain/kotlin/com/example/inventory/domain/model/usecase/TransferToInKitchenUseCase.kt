package com.example.inventory.domain.model.usecase

import com.example.inventory.model.WarehouseItem
import com.example.inventory.data.model.repository.WarehouseRepository

/**
 * ✅ Use case to handle transferring a single warehouse item to the in_kitchen table.
 * This calls the new transferToInKitchenSingle() method from WarehouseRepository.
 */
class TransferToInKitchenUseCase(
    private val repository: WarehouseRepository
) {

    suspend operator fun invoke(
        item: WarehouseItem,
        quantity: Double,
        unit: String,
        shelfLifeValue: Double?,
        shelfLifeUnit: String?,
        prepMethod: String?,
        expiryIso: String? = null,
        servingSize: Double? = null,
        expiryBasedOnManufacturer: Boolean = false
    ): Boolean {
        return repository.transferToInKitchenSingle(
            warehouseItem = item,
            transferQuantity = quantity,
            unit = unit,
            shelfLifeValue = shelfLifeValue,
            shelfLifeUnit = shelfLifeUnit,
            preparationMethod = prepMethod,
            expiryIso = expiryIso,
            servingSize = servingSize,
            expiryBasedOnManufacturer = expiryBasedOnManufacturer
        )
    }
}
