package com.example.inventory.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDTO(
    @SerialName("variantid") val variantId: Long,
    @SerialName("productid") val productId: Long,
    @SerialName("productname") val productName: String,
    @SerialName("variantname") val variantName: String,
    @SerialName("price") val price: Double,
    @SerialName("category_id") val categoryId: Long?,
    @SerialName("imageurl") val imageUrl: String? = null,
    @SerialName("categoryname") val categoryName: String? = null
)