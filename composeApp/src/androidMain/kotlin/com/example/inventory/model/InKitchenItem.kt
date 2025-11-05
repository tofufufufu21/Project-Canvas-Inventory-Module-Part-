package com.example.inventory.model

import kotlinx.serialization.Serializable

@Serializable
data class InKitchenItem(
    val id: String? = null,
    val warehouse_item_id: Long? = null,
    val product_name: String,
    val product_image_url: String? = null,
    val category_type: String,
    val sub_category: String? = null,
    val batch_number: String,
    val preparation_method: String,
    val original_quantity: Double,
    val current_quantity: Double,
    val reserved_quantity: Double = 0.0,
    val available_quantity: Double? = null,
    val unit: String,
    val serving_size: Double? = null,
    val shelf_life_value: Double? = null,
    val shelf_life_unit: String? = null,
    val expiry_based_on_manufacturer: Boolean? = null,
    val original_expiry_date: String? = null,
    val calculated_expiry_date: String? = null,
    val status: String = "available",
    val transferred_at: String? = null,
    val transfer_reason: String? = null,
    val disposed_at: String? = null,
    val disposal_reason: String? = null,
    val returned_to_warehouse: Boolean? = null,
    val returned_at: String? = null,
    val return_reason: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val deleted_at: String? = null
)