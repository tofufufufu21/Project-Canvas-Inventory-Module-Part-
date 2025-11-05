package com.example.inventory.pos

data class PaymentState(
    val method: PaymentMethod = PaymentMethod.Cash,
    val tenderedAmount: Double = 0.0,
    val change: Double = 0.0,
    val isValid: Boolean = false
)

