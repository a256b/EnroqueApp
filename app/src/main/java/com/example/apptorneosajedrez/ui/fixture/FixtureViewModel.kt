package com.example.apptorneosajedrez.ui.fixture

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FixtureViewModel : ViewModel() {
    private val _torneosConEditarOculto = MutableStateFlow<Set<String>>(emptySet())
    val torneosConEditarOculto = _torneosConEditarOculto.asStateFlow()

    private val _torneosConIniciarOculto = MutableStateFlow<Set<String>>(emptySet())
    val torneosConIniciarOculto = _torneosConIniciarOculto.asStateFlow()

    fun ocultarBotonEditar(idTorneo: String) {
        _torneosConEditarOculto.update { it + idTorneo }
    }

    fun ocultarBotonIniciarTorneo(idTorneo: String) {
        _torneosConIniciarOculto.update { it + idTorneo }
    }
}
