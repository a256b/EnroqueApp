package com.example.apptorneosajedrez.model

import com.google.firebase.Timestamp

data class Movimiento(
    val indice: Long = 0L,                  // número de movimiento individual 1,2,3,4,...  (ply)
    val id: String = "",
    val numeroMovimientoCompleto: Int = 0,  // número de movimiento de ambos colores 1,1,2,2,3,3,...
    val color: String = "BLANCAS",
    val notacion: String = "",
    val desde: String? = null,
    val hasta: String? = null,
    val pieza: String? = null,
    val esCaptura: Boolean = false,
    val esJaque: Boolean = false,
    val esMate: Boolean = false,
    val creadoEn: Timestamp? = null
)
