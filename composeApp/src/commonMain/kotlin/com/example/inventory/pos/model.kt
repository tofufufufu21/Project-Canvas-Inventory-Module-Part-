package com.example.inventory.pos

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: Category,
    val stock: Int
)

enum class Category {
    Drinks,
    Food,
    Snacks
}

data class CartItem(
    val product: Product,
    val qty: Int
)

enum class OrderType {
    DineIn,
    TakeOut
}

enum class PaymentMethod {
    Cash,
    OnlineQR
}

data class CashPayment(
    val tendered: Double = 0.0,
    val change: Double = 0.0
)

data class PaymentState(
    val method: PaymentMethod = PaymentMethod.Cash,
    val cash: CashPayment = CashPayment(),
    val isValid: Boolean = false
)