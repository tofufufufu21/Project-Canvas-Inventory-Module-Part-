package com.example.inventory.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDTO(
    val id: Long,
    val name: String
)
