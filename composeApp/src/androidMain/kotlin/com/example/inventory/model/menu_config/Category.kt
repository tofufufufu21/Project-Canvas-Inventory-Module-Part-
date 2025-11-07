package com.example.inventory.model.menu_config

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Long,
    val name: String,
    val description: String? = null,
    val created_at: String? = null
)
