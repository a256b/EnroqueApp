package com.example.apptorneosajedrez.ui.fixture

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FixtureViewModel : ViewModel() {
    private val _debeOcultarEditar = MutableStateFlow(false)
    val debeOcultarEditar = _debeOcultarEditar.asStateFlow()

    private val _debeOcultarIniciarTorneo = MutableStateFlow(false)
    val debeOcultarIniciarTorneo = _debeOcultarIniciarTorneo.asStateFlow()

    fun ocultarBotonEditar() {
        _debeOcultarEditar.value = true
    }

    fun ocultarBotonIniciarTorneo() {
        _debeOcultarIniciarTorneo.value = true
    }
}
