package com.example.apptorneosajedrez.ui.register

import android.util.Log
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

    private companion object {
        const val TAG = "RegisterViewModel"
    }

    private val _uiState = MutableLiveData(RegisterUiState())
    val uiState: LiveData<RegisterUiState> = _uiState

    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            Log.d(TAG, "register() - Todos los campos son obligatorios")
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
                val mensajeErrorTraducido = traducirMensajeErrorRegistro(e)

                _uiState.value = RegisterUiState(
                    isLoading = false,
                    errorMessage = mensajeErrorTraducido
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

    private fun traducirMensajeErrorRegistro(e: Exception): String {
        Log.d(TAG, "traducirMensajeErrorRegistro() - ${e.message}")
        val msg = e.message ?: return "No se pudo crear la cuenta"

        return when {
            msg.contains("The email address is badly formatted", ignoreCase = true) ->
                "El correo ingresado no tiene un formato válido"

            msg.contains("email address is already in use", ignoreCase = true) ||
                    msg.contains("ERROR_EMAIL_ALREADY_IN_USE", ignoreCase = true) ->
                "Este correo ya se encuentra registrado"

            msg.contains("Password should be at least", ignoreCase = true) ||
                    msg.contains("WEAK_PASSWORD", ignoreCase = true) ->
                "La contraseña debe tener al menos 6 caracteres"

            msg.contains("network error", ignoreCase = true) ||
                    msg.contains("ERROR_NETWORK_REQUEST_FAILED", ignoreCase = true) ->
                "Error de conexión. Verifica tu internet"

            msg.contains("ERROR_TOO_MANY_REQUESTS", ignoreCase = true) ||
                    msg.contains("too many attempts", ignoreCase = true) ->
                "Demasiados intentos. Intenta nuevamente más tarde"

            msg.contains("ERROR_USER_DISABLED", ignoreCase = true) ->
                "Esta cuenta ha sido deshabilitada"

            else ->
                "No se pudo crear la cuenta. Intenta nuevamente"
        }
    }

}
