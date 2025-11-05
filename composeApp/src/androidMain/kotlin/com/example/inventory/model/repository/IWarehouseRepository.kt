package com.example.inventory.model.repository

import com.example.inventory.model.WarehouseItem

/**
 * Defines the contract for a repository that can fetch warehouse items.
 * This interface is in commonMain and can be used by shared ViewModels.
 */
interface IWarehouseRepository {
    suspend fun fetchWarehouseItems(): List<WarehouseItem>
}