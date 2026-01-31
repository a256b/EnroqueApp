package com.example.apptorneosajedrez.ui.detalleJugador

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.ui._theme.AppTorneosTheme

class DetalleJugadorFragment : Fragment(R.layout.fragment_detalle_jugador) {

    private val viewModel: DetalleJugadorViewModel by lazy {
        val jugadorId = requireArguments().getString("jugadorId")
            ?: throw IllegalStateException("jugadorId no encontrado en argumentos")

        ViewModelProvider(
            this,
            DetalleJugadorViewModelFactory(jugadorId = jugadorId)
        )[DetalleJugadorViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val composeView = view.findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            AppTorneosTheme {
                DetalleJugadorScreen(viewModel = viewModel)
            }
        }
    }
}
