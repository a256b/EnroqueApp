package com.example.apptorneosajedrez.ui.detallejugador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apptorneosajedrez.data.JugadorRepository

class DetalleJugadorViewModelFactory(
    private val jugadorId: String,
    private val jugadorRepository: JugadorRepository = JugadorRepository()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetalleJugadorViewModel::class.java)) {
            return DetalleJugadorViewModel(
                jugadorRepository = jugadorRepository,
                jugadorId = jugadorId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}