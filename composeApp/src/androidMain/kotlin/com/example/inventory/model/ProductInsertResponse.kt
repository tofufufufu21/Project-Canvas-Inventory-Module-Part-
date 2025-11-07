package com.example.inventory.model



import kotlinx.serialization.Serializable

@Serializable
data class ProductInsertResponse(
    val id: Long
)
