package com.example.inventory.data.auth

import kotlinx.coroutines.delay

/**
 * No-network demo auth repo.
 * Accepts any non-empty email/password and returns a fake token string.
 */
class DemoAuthRepository {
    suspend fun signIn(email: String, password: String): Result<String> {
        delay(450) // simulate latency
        return if (email.isNotBlank() && password.isNotBlank()) {
            Result.success("demo_token_${email.hashCode()}")
        } else {
            Result.failure(Exception("Please enter email and password"))
        }
    }
}
