package com.example.apptorneosajedrez.model

enum class TipoUsuario {
    AFICIONADO,
    JUGADOR,
    ORGANIZADOR
}

enum class EstadoComoJugador {
    NINGUNO,
    PENDIENTE,
    ACEPTADO,
    RECHAZADO
}

data class Usuario(
    val uid: String = "",
    val nombreCompleto: String? = "",
    val email: String = "",
    val tipoUsuario: TipoUsuario = TipoUsuario.AFICIONADO,
    val estadoComoJugador: EstadoComoJugador = EstadoComoJugador.NINGUNO
)