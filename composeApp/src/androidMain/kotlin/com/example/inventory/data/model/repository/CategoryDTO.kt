package com.example.inventory.data.model.repository

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDTO(
    val id: Long? = null,
    val name: String,
    val description: String? = null // ✅ Add this field
)
