package com.example.inventory.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TransferHistory(
    val id: Long? = null,
    val warehouse_item_id: Long?,
    val product_name: String,
    val transfer_quantity: Double,
    val unit: String?,
    val preparation_method: String?,
    val shelf_life_value: Double? = null,
    val shelf_life_unit: String? = null,
    val expiry_iso: String? = null,
    val transferred_at: String // ISO timestamp string
)
