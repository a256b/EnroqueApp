package com.example.apptorneosajedrez.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.model.Usuario
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: Usuario? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(LoginUiState())
    val uiState: LiveData<LoginUiState> = _uiState

    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(
                isLoading = false,
                errorMessage = "El correo electrónico y la contraseña son obligatorios"
            )
            return
        }

        _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val user = authRepository.loginWithEmail(email, password)
                _uiState.value = LoginUiState(
                    isLoading = false,
                    errorMessage = null,
                    loggedInUser = user
                )
            } catch (e: Exception) {
                val mensajeErrorTraducido = traducirMensajeError(e)

                _uiState.value = LoginUiState(
                    isLoading = false,
                    errorMessage = mensajeErrorTraducido

                )
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val user = authRepository.loginWithGoogle(idToken)
                _uiState.value = LoginUiState(
                    isLoading = false,
                    errorMessage = null,
                    loggedInUser = user
                )
            } catch (e: Exception) {
                _uiState.value = LoginUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al iniciar sesión con Google"
                )
            }
        }
    }

    private fun traducirMensajeError(e: Exception): String {
        val msg = e.message ?: return "Error al iniciar sesión"

        return when {
            msg.contains("The email address is badly formatted", ignoreCase = true) ||
                    msg.contains("There is no user record", ignoreCase = true) ||
                    msg.contains("The password is invalid", ignoreCase = true) ->
                "El correo electrónico y/o la contraseña son incorrectos"
            msg.contains("blocked all requests", ignoreCase = true) ->
                "Demasiados intentos fallidos. Intenta nuevamente más tarde."

            else -> "Error al iniciar sesión: ${e.message}"
        }
    }

}
