package com.example.apptorneosajedrez.ui.detallejugador

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptorneosajedrez.data.JugadorRepository
import com.example.apptorneosajedrez.ui.detalleJugador.DetalleJugadorUiState
import kotlinx.coroutines.launch

class DetalleJugadorViewModel(
    private val jugadorRepository: JugadorRepository,
    private val jugadorId: String
) : ViewModel() {

    private val _uiState = MutableLiveData(DetalleJugadorUiState())
    val uiState: LiveData<DetalleJugadorUiState> = _uiState

    init {
        cargarJugador()
    }


    /**
     * Carga los datos del jugador en base al jugadorId.
     */
    private fun cargarJugador() {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(
                isLoading = true,
                error = null
            )

            try {
                val jugador = jugadorRepository.obtenerJugadorPorId(jugadorId)

                if (jugador != null) {
                    _uiState.value = DetalleJugadorUiState(
                        isLoading = false,
                        nombre = jugador.nombre,
                        email = jugador.email,
                        error = null
                    )
                } else {
                    _uiState.value = DetalleJugadorUiState(
                        isLoading = false,
                        nombre = "",
                        email = "",
                        tipoUsuario = "",
                        error = "No se encontró el jugador"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    error = "Error al cargar datos del jugador: ${e.message}"
                )
            }
        }
    }
}
