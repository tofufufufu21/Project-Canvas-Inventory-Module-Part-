package com.example.inventory.data.model.repository

import com.example.inventory.data.model.WarehouseItem

/**
 * Defines the contract for a repository that can fetch warehouse items.
 * This interface is in commonMain and can be used by shared ViewModels.
 */
interface IWarehouseRepository {
    suspend fun fetchWarehouseItems(): List<WarehouseItem>
}