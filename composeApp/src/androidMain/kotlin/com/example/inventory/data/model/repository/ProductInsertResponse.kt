package com.example.inventory.data.model.repository

import kotlinx.serialization.Serializable

@Serializable
data class ProductInsertResponse(
    val id: Long
)