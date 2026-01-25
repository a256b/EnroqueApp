package com.example.apptorneosajedrez.model

import java.io.Serializable

enum class EstadoInscripcion {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA
}

data class Inscripcion(
    val id: String = "",
    val idJugador: String = "",
    val idTorneo: String = "",
    val estado: EstadoInscripcion = EstadoInscripcion.PENDIENTE
) : Serializable
