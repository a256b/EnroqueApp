package com.example.apptorneosajedrez.ui.perfil

import com.example.apptorneosajedrez.model.EstadoComoJugador
import com.example.apptorneosajedrez.model.TipoUsuario

data class PerfilUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userEmail: String = "",
    val userType: String = "",
    val error: String? = null
)