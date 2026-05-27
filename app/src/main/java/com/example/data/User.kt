package com.imnotesminimal.app.data

data class User(
    val email: String,
    val displayName: String,
    val imageUrl: String? = null,
    val accountType: String = "EMAIL" // "EMAIL" or "GOOGLE"
)
