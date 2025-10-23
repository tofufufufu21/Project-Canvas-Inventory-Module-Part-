package com.example.inventory.pos

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
actual fun PlatformDateDisplay() {
    val currentDate = Date()
    val formatter = SimpleDateFormat("EEEE, d MMM yyyy", Locale.ENGLISH)
    val formattedDate = formatter.format(currentDate)
    Text(
        text = formattedDate,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
    )
}