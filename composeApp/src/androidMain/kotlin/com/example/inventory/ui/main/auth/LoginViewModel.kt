package com.example.inventory.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay

class DemoLoginViewModel : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var isLoggedIn by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var accessToken by mutableStateOf<String?>(null)

    suspend fun signIn() {
        isLoading = true
        errorMessage = null
        delay(800) // simulate login delay

        if (email.trim() == "admin" && password.trim() == "admin123") {
            isLoggedIn = true
            accessToken = "demo_token_123"
        } else {
            errorMessage = "Invalid username or password."
            isLoggedIn = false
        }

        isLoading = false
    }

    fun logout() {
        isLoggedIn = false
        accessToken = null
        email = ""
        password = ""
    }
}
