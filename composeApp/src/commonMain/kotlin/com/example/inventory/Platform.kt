package com.example.inventory

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform