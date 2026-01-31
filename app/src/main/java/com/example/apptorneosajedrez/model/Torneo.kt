package com.example.apptorneosajedrez.model
import java.io.Serializable

data class Torneo(
    val idTorneo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val horaInicio: String = "",
    val ubicacion: String = "",
    val estado: EstadoTorneo = EstadoTorneo.PROXIMO,
    val partidas: List<Partida> = emptyList(),
    val jugadores: List<String> = emptyList() // Solo IDs
) : Serializable
