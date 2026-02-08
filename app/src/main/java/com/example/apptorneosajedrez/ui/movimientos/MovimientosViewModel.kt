package com.example.apptorneosajedrez.ui.movimientos

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.data.MovimientosRepository
import com.example.apptorneosajedrez.data.TorneoRepository
import com.example.apptorneosajedrez.model.EstadoPartida
import com.example.apptorneosajedrez.model.Movimiento
import com.example.apptorneosajedrez.model.MovimientoFila
import com.example.apptorneosajedrez.model.TipoUsuario
import com.google.firebase.firestore.ListenerRegistration


class MovimientosViewModel(
    private val torneoId: String,
    private val partidaId: String,
    private val movimientosRepository: MovimientosRepository,
    private val authRepository: AuthRepository,
    private val torneoRepository: TorneoRepository
) : ViewModel() {

    private companion object {
        const val TAG = "MovimientosViewModel"
    }

    private var listenerRegistration: ListenerRegistration? = null

    private val _uiState = MutableLiveData(
        MovimientosUiState(
            filasMovimientos = emptyList(),
            nuevoMovimiento = "",
            usuarioPuedeCargarMovimientos = false
        )
    )
    val uiState: LiveData<MovimientosUiState> = _uiState

    init {
        cargarMovimientos()
        determinarPermisos()
    }

    private fun determinarPermisos() {
        val usuario = authRepository.getCurrentUserInMemory()
        val esOrganizador = usuario?.tipoUsuario == TipoUsuario.ORGANIZADOR

        Log.d(TAG, "esOrganizador: $esOrganizador")

        if (torneoId.isNullOrEmpty() || partidaId.isNullOrEmpty()) {
            Log.w(TAG, "determinarPermisos: ids inválidos torneoId=$torneoId partidaId=$partidaId")
            _uiState.value = _uiState.value?.copy(
                usuarioPuedeCargarMovimientos = false
            )
            return
        }

        torneoRepository.obtenerPartida(torneoId, partidaId) { partida ->
            val partidaEnCurso = partida?.estado == EstadoPartida.EN_CURSO

            Log.d(TAG, "partida: $partida")

            _uiState.value = _uiState.value?.copy(
                usuarioPuedeCargarMovimientos = esOrganizador && partidaEnCurso
            )
        }
    }


    private fun cargarMovimientos() {
        _uiState.value = _uiState.value?.copy(
            loading = true,
            errorMessage = null
        )

        movimientosRepository.escucharMovimientos(
            torneoId = torneoId,
            partidaId = partidaId,
            onResult = { lista ->
                _uiState.postValue(
                    _uiState.value?.copy(
                        filasMovimientos = lista.toFilasDeMovimientos(partidaId),
                        colorTurno = proximoColor(lista),
                        loading = false,
                        errorMessage = null
                    )
                )
            },
            onError = { e ->
                _uiState.postValue(
                    _uiState.value?.copy(
                        loading = false,
                        errorMessage = e.message ?: "Error al cargar los movimientos"
                    )
                )
            }
        )
    }

    /**
     * Agrupa movimientos por número de movimiento completo y arma las filas
     * que necesita la UI (numero, blancas, negras).
     */
    private fun List<Movimiento>.toFilasDeMovimientos(
        partidaId: String
    ): List<MovimientoFila> {

        return this
            .groupBy { it.numeroMovimientoCompleto }
            .map { (numero, movimientosDeTurno) ->
                val movBlancas = movimientosDeTurno
                    .find { it.color.uppercase() == "BLANCAS" }
                val movNegras = movimientosDeTurno
                    .find { it.color.uppercase() == "NEGRAS" }

                MovimientoFila(
                    id = "${partidaId}_$numero",
                    numero = numero,
                    blancas = movBlancas?.notacion.orEmpty(),
                    negras = movNegras?.notacion
                )
            }
            .sortedBy { it.numero }
    }

    private val CHESSMOVEREGEX = Regex(
        """^(?:O-O(?:-O)?|[RDTACNBKQ]?(?:[a-h][1-8]?|[1-8])?x?[a-h][1-8](?:=[RDTACNBKQ])?)[+#]?(?:[!?]{1,2})?$"""
    )

    private fun movimientoValido(textoMovimiento: String): Boolean =
        CHESSMOVEREGEX.matches(textoMovimiento.trim())

    fun onMovimientoChange(textoMovimiento: String) {
        val texto = textoMovimiento.trim()
        val estadoActual = _uiState.value ?: return

        _uiState.value = estadoActual.copy(
            nuevoMovimiento = texto,
            botonEnviarMovimientoHabilitado = texto.isNotBlank() && movimientoValido(texto)
        )
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun onBorrarTextoClick() {
        _uiState.value = _uiState.value?.copy(nuevoMovimiento = "")
        _uiState.value = _uiState.value?.copy(botonEnviarMovimientoHabilitado = false)
    }

    fun onEnviarMovimientoClick() {

        Log.d(TAG, "onEnviarMovimientolick()")

        val estadoActual = _uiState.value ?: return
        val textoMovimiento = estadoActual.nuevoMovimiento.trim()

        if (textoMovimiento.isBlank()) return
        if (!estadoActual.usuarioPuedeCargarMovimientos) return


        guardarMovimientoEnFirestore(textoMovimiento)
    }

    private fun guardarMovimientoEnFirestore(notacion: String) {
        _uiState.value = _uiState.value?.copy(
            loading = true,
            errorMessage = null
        )

        movimientosRepository.agregarMovimiento(
            torneoId = torneoId,
            partidaId = partidaId,
            notacion = notacion
        ) { exito ->
            if (exito) {
                _uiState.postValue(
                    _uiState.value?.copy(
                        nuevoMovimiento = "",
                        botonEnviarMovimientoHabilitado = false,
                        //colorTurno = siguienteColor(_uiState.value?.colorTurno),
                        loading = false
                    )
                )

            } else {
                _uiState.postValue(
                    _uiState.value?.copy(
                        loading = false,
                        errorMessage = "No se pudo guardar el movimiento"
                    )
                )
            }
        }
    }

    fun onDeshacerMovimientoClick() {
        movimientosRepository.borrarUltimoMovimiento(
            torneoId = torneoId,
            partidaId = partidaId
        )
        { exito ->
            if (!exito) {
                // Aquí podrías actualizar el uiState con un mensaje de error o loguear
                // _uiState.postValue(_uiState.value?.copy(mensajeError = "No se pudo deshacer el último movimiento"))
            }
            // Si tenés escucha en tiempo real, no hace falta tocar la lista de movimientos:
            // la actualización vendrá sola desde Firestore.
        }
    }


    private fun proximoColor(lista: List<Movimiento>): String {
        return when (lista.lastOrNull()?.color) {
            "BLANCAS" -> "NEGRAS"
            "NEGRAS" -> "BLANCAS"
            else -> "BLANCAS" // lista vacía o color inesperado
        }
    }
}
