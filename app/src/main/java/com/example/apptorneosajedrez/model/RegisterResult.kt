package com.example.apptorneosajedrez.model

data class RegisterResult(
    val success: Usuario? = null,
    val error: String? = null,
    val loading: Boolean = false
)