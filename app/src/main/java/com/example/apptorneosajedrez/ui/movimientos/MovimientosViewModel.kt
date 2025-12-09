package com.example.apptorneosajedrez.ui.movimientos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apptorneosajedrez.data.MovimientosRepository
import com.example.apptorneosajedrez.model.Movimiento

class MovimientosViewModel : ViewModel() {
    private val repo = MovimientosRepository()
    private val _moves = MutableLiveData<List<Movimiento>>(emptyList())
    val moves: LiveData<List<Movimiento>> = _moves

    init {
        repo.listenMoves { list ->
            _moves.postValue(list)
        }
    }

    fun sendMove(notation: String) {
        if (notation.isNotBlank()) {
            repo.sendMove(notation)
        }
    }
}
