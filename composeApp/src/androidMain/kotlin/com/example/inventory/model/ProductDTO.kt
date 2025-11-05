package com.example.inventory.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductDTO(
    val variantId: Long,          // ✅ used in PosViewModel
    val productId: Long,
    val productName: String,
    val variantName: String = "",
    val price: Double,
    val categoryId: Long?,
    val imageUrl: String? = null,
    val categoryName: String? = null
)
