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
    private fun formatUserType(tipo: TipoUsuario?): String {
        return when (tipo?.toString()?.uppercase()) {
            "ORGANIZADOR" -> "Organizador/a"
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
                            userType = formatUserType(usuario.tipoUsuario),
                            detallePermisos = obtenerDetallePermisos(usuario.tipoUsuario),
                            error = null,
                        )
                    } else {
                        _uiState.value = PerfilUiState(
                            isLoading = false,
                            userName = "",
                            userEmail = "",
                            userType = formatUserType(TipoUsuario.AFICIONADO),
                            detallePermisos = "",
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

    private fun obtenerDetallePermisos(tipoUsuario: TipoUsuario): String {
        val permisosBase = """
        Visualización y seguimiento de torneos
        Visualización de partidas
        Visualización de jugadores
        Visualización de movimientos de partidas (en vivo)
        Acceder y utilizar el mapa interactivo
        Consultar su perfil de usuario
    """.trimIndent()

        val permisosAdicionales = when (tipoUsuario) {
            TipoUsuario.AFICIONADO -> """
            Solicitar alta como Jugador
            """.trimIndent()

            TipoUsuario.ORGANIZADOR -> """
            Gestión de torneos
            Gestión de altas de jugadores
            Gestión de inscripciones a torneos
            Gestión de partidas
            Ingreso de movimientos de partidas en directo
            """.trimIndent()

            TipoUsuario.JUGADOR -> """
            Consultar sus inscripciones
            Gestión de inscripciones a torneos
            Gestión de partidas
            """.trimIndent()
        }

        return "$permisosBase\n$permisosAdicionales".trim()
    }

}
