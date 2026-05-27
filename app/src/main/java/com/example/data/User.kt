package com.example.data

data class User(
    val email: String,
    val displayName: String,
    val imageUrl: String? = null,
    val accountType: String = "EMAIL" // "EMAIL" or "GOOGLE"
)
