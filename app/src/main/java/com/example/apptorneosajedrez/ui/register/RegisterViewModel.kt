package com.example.apptorneosajedrez.ui.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.model.Usuario
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registeredUser: Usuario? = null
)

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(RegisterUiState())
    val uiState: LiveData<RegisterUiState> = _uiState

    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.value = RegisterUiState(
                errorMessage = "Todos los campos son obligatorios"
            )
            return
        }

        if (!isEmailValid(email)) {
            _uiState.value = RegisterUiState(
                errorMessage = "Formato de correo inválido"
            )
            return
        }

        if (!isPasswordValid(password)) {
            _uiState.value = RegisterUiState(
                errorMessage = "Contraseña inválida. Debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número"
            )
            return
        }

        if (password != confirmPassword) {
            _uiState.value = RegisterUiState(
                errorMessage = "Las contraseñas no coinciden"
            )
            return
        }

        _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val user = authRepository.registerWithEmail(
                    fullName = fullName,
                    email = email,
                    password = password
                )
                _uiState.value = RegisterUiState(
                    isLoading = false,
                    registeredUser = user
                )
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al registrar usuario"
                )
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        val passwordRegex = Regex(
            pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
        )
        return passwordRegex.matches(password)
    }
}
