package com.example.apptorneosajedrez.model
import java.io.Serializable

enum class EstadoPartida {
    PENDIENTE,
    EN_CURSO,
    FINALIZADA
}

data class Partida(
    val id: String = "",
    val idJugador1: String? = "", // Blancas
    val idJugador2: String? = "", // Negras
    val estado: EstadoPartida = EstadoPartida.PENDIENTE,
    val ganador: String? = "",    // ID del jugador ganador
    val fecha: String? = "",
    val hora: String? = ""
) : Serializable
