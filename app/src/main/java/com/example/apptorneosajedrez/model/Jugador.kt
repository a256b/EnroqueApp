package com.example.apptorneosajedrez.model

import java.io.Serializable

data class Jugador(
    val id: String = "",
    val nombre: String = "",
    val email: String = ""
) : Serializable
