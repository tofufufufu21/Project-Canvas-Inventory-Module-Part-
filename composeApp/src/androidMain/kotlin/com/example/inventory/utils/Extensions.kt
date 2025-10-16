package com.example.inventory.utils

import java.text.DecimalFormat

/** Return display-friendly string for nullable strings */
fun String?.orDash(): String = this ?: "—"

/** Format double to 2 decimal places (or drop decimals if integer) */
fun Double.formatQuantity(): String {
    return if (this % 1.0 == 0.0) {
        this.toInt().toString()
    } else {
        DecimalFormat("#.##").format(this)
    }
}
