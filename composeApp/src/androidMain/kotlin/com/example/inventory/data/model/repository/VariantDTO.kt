package com.example.inventory.data.model.repository

import kotlinx.serialization.Serializable

@Serializable
data class VariantDTO(
    val id: Long,
    val product_id: Long,
    val variant_name: String,
    val price: Double
)