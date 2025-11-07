package com.example.inventory.model

import kotlinx.serialization.Serializable

/**
 * Used ONLY for inserting new records into the menu_products table.
 * Matches exactly the schema of that table in Supabase.
 */
@Serializable
data class ProductInsertDTO(
    val category_id: Long?,
    val product_name: String,
    val image_url: String? = null,
    val description: String? = null
)