package com.example.apptorneosajedrez.ui.perfil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.model.TipoUsuario
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(PerfilUiState())
    val uiState: LiveData<PerfilUiState> = _uiState

    init {
        loadUserData()
    }

    /**
     * Convierte el tipo de usuario desde el formato de base de datos
     * al formato legible para mostrar en la UI
     */
    private fun formatUserType(tipo: String?): String {
        return when (tipo?.uppercase()) {
            "ADMINISTRADOR" -> "Administrador/a"
            "JUGADOR" -> "Jugador/a"
            else -> "Aficionado/a"
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value?.copy(isLoading = true)

                // Observar cambios en el usuario actual
                authRepository.currentUser.collect { usuario ->
                    if (usuario != null) {
                        _uiState.value = PerfilUiState(
                            isLoading = false,
                            userName = usuario.nombreCompleto ?: "Usuario sin nombre",
                            userEmail = usuario.email ?: "",
                            userType = formatUserType((usuario.tipoUsuario ?: TipoUsuario.AFICIONADO).toString()),
                            error = null


                        )
                    } else {
                        _uiState.value = PerfilUiState(
                            isLoading = false,
                            userName = "",
                            userEmail = "",
                            userType = formatUserType(( TipoUsuario.AFICIONADO).toString()),
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

    fun refreshUserData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value?.copy(isLoading = true)
                authRepository.refreshCurrentUser()
                // El collect en loadUserData actualizará automáticamente el estado
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    error = "Error al actualizar datos: ${e.message}"
                )
            }
        }
    }
}