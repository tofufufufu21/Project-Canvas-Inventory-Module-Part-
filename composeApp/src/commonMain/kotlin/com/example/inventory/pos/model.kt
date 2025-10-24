package com.example.inventory.pos


//
// ---------- POS MODELS ----------
//

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: PosCategory,
    val stock: Int
)

// Represents an item currently in the POS cart
data class CartItem(
    val product: Product,
    val qty: Int
)

// Defines order type for a sale
enum class OrderType {
    DineIn,
    TakeOut
}

// Payment methods supported
enum class PaymentMethod {
    Cash,
    OnlineQR
}

// Data model for cash-based payments
data class CashPayment(
    val tendered: Double = 0.0,
    val change: Double = 0.0
)

// Combined state of payment selection in POS
data class PaymentState(
    val method: PaymentMethod = PaymentMethod.Cash,
    val cash: CashPayment = CashPayment(),
    val isValid: Boolean = false
)