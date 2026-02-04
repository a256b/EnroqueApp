package com.example.apptorneosajedrez.model
import java.io.Serializable
import com.google.firebase.Timestamp

enum class EstadoTorneo{
    PROXIMO,
    ACTIVO,
    FINALIZADO,
    SUSPENDIDO
}

data class Torneo(
    val idTorneo: String ="",

    val nombre: String = "",
    val descripcion: String = "",

    val fechaInicio: String = "",
    val fechaFin: String = "",
    val horaInicio: String = "",

    val ubicacion: String = "",
    val estado: EstadoTorneo = EstadoTorneo.PROXIMO,

    val partidas: List<Partida> = emptyList(),
    val jugadores: List<String> = emptyList(), // Solo IDs

    val creadoEn: Timestamp? = null

) : Serializable
