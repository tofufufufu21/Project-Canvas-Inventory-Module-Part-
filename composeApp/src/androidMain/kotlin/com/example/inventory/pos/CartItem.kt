package com.example.inventory.pos

import com.example.inventory.model.ProductDTO

data class CartItem(
    val product: ProductDTO,
    var quantity: Int
)
