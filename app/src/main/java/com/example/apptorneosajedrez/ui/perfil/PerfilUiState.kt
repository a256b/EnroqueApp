package com.example.apptorneosajedrez.ui.perfil

data class PerfilUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userEmail: String = "",
    val userType: String = "",
    val error: String? = null,
    val detallePermisos: String? = ""
)