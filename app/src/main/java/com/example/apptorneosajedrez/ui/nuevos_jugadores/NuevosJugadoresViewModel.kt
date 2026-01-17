package com.example.apptorneosajedrez.ui.nuevos_jugadores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.model.EstadoComoJugador
import com.example.apptorneosajedrez.model.Usuario
import kotlinx.coroutines.launch

class NuevosJugadoresViewModel : ViewModel() {

    private val repository = AuthRepository.getInstance()

    private val _usuariosPendientes = MutableLiveData<List<Usuario>>()
    val usuariosPendientes: LiveData<List<Usuario>> get() = _usuariosPendientes

    fun cargarPendientes() {
        viewModelScope.launch {
            val todos = repository.obtenerTodosLosUsuarios()

            val pendientes = todos.filter {
                it.estadoComoJugador == EstadoComoJugador.PENDIENTE
            }

            _usuariosPendientes.value = pendientes
        }
    }

    fun actualizarEstado(usuario: Usuario, nuevoEstado: EstadoComoJugador) {
        viewModelScope.launch {
            // Actualizamos en Firestore / repositorio
            repository.actualizarEstadoUsuario(usuario.uid, nuevoEstado)

            // Recargamos la lista de pendientes
            cargarPendientes()
        }
    }
}
