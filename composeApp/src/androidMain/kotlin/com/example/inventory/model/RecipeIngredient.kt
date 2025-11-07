package com.example.inventory.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeIngredient(
    val id: Long? = null,
    val ingredient_id: Long,
    val variant_id: Long? = null, // ✅ Add this field
    val measurement_value: Double,
    val measurement_unit: String
)
