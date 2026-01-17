package com.example.apptorneosajedrez.ui.perfil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.model.TipoUsuario
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class PerfilViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(PerfilUiState())
    val uiState: LiveData<PerfilUiState> = _uiState

    init {
        observeUserInMemory()
    }

    /**
     * Convierte el tipo de usuario desde el formato de base de datos
     * al formato legible para mostrar en la UI.
     */
    private fun formatUserType(tipo: String?): String {
        return when (tipo?.uppercase()) {
            "ADMINISTRADOR" -> "Administrador/a"
            "JUGADOR" -> "Jugador/a"
            else -> "Aficionado/a"
        }
    }

    /**
     * Observa el usuario en memoria expuesto por AuthRepository.currentUser.
     * No hace ninguna llamada a Firebase. El repositorio se encarga de
     * mantener sincronizado ese flujo con Firestore.
     */
    private fun observeUserInMemory() {
        viewModelScope.launch {
            // Estado inicial: cargando
            _uiState.value = _uiState.value?.copy(
                isLoading = true,
                error = null
            )

            try {
                authRepository.currentUser.collectLatest { usuario ->
                    if (usuario != null) {
                        _uiState.value = PerfilUiState(
                            isLoading = false,
                            userName = usuario.nombreCompleto ?: "Usuario sin nombre",
                            userEmail = usuario.email,
                            userType = formatUserType(
                                usuario.tipoUsuario.toString()
                            ),
                            error = null
                        )
                    } else {
                        _uiState.value = PerfilUiState(
                            isLoading = false,
                            userName = "",
                            userEmail = "",
                            userType = formatUserType(TipoUsuario.AFICIONADO.toString()),
                            error = "No hay usuario autenticado"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    error = "Error al cargar datos: ${e.message}"
                )
            }
        }
    }
}
