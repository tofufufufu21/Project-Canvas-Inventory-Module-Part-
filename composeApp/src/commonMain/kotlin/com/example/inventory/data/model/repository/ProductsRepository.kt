package com.example.inventory.data.model.repository

import com.example.inventory.pos.Product

/**
 * Defines the contract for a repository that can fetch product data for the POS.
 * This interface is in commonMain and can be used by shared ViewModels.
 */
interface ProductsRepository {
    suspend fun getProductsForPOS(): List<Product>
}