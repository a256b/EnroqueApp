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
                errorMessage = "Correo y contraseña son obligatorios"
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
                _uiState.value = LoginUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al iniciar sesión"

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
}
