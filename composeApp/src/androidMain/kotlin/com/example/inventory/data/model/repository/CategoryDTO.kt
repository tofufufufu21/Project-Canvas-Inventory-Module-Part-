package com.example.inventory.data.model.repository

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDTO(
    val id: Long,
    val name: String
)