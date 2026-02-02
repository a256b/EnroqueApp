package com.example.apptorneosajedrez.model
import java.io.Serializable

enum class EstadoPartida {
    PENDIENTE,
    EN_CURSO,
    FINALIZADA
}

enum class Fase{
    CUARTOS,
    SEMI,
    FINAL
}

data class Partida(
    val idPartida: String = "",
    val idJugador1: String? = "", // Blancas
    val idJugador2: String? = "", // Negras
    val estado: EstadoPartida = EstadoPartida.PENDIENTE,
    val fase: Fase? = null,
    val ganador: String? = "",    // ID del jugador ganador
    val fecha: String? = "",
    val hora: String? = ""
) : Serializable
