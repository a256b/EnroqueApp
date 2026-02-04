package com.example.apptorneosajedrez.ui.movimientos

import com.example.apptorneosajedrez.model.MovimientoFila

data class MovimientosUiState(
    val filasMovimientos: List<MovimientoFila> = emptyList(),
    val nuevoMovimiento: String = "",

    val usuarioPuedeCargarMovimientos: Boolean = true,

    val colorTurno: String = "BLANCAS",
    val botonEnviarMovimientoHabilitado: Boolean = false,

    val loading: Boolean = true,
    val errorMessage: String? = null
)
