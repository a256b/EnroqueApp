package com.example.apptorneosajedrez.ui.detalleJugador

data class DetalleJugadorUiState(
    val isLoading: Boolean = true,
    val nombre: String = "",
    val email: String = "",
    val tipoUsuario: String = "",
    val error: String? = null
)
