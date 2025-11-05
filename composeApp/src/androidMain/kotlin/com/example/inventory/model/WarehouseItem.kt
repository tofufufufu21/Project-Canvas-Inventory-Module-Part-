package com.example.inventory.model

import kotlinx.serialization.Serializable

@Serializable
data class WarehouseItem(
    val id: Long? = null,
    val product_name: String,
    val product_image_url: String? = null,
    val category_type: String,
    val sub_category: String? = null,
    val quantity: Double,
    val unit: String,
    val preparation_method: String,
    val has_expiry: Boolean = false,
    val expiry_date: String? = null,
    val date_created: String? = null,
    val idempotency_key: String? = null,
    val created_by: String? = null,
    val notes: String? = null,
    val serving_size: Double? = null,
    val shelf_life_value: Double? = null,
    val shelf_life_unit: String? = null
)