package com.example.apptorneosajedrez.ui.perfil

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.ui.compose_theme.AppTorneosTheme

class PerfilFragment : Fragment(R.layout.fragment_perfil) {

    private val viewModel: PerfilViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val composeView = view.findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            AppTorneosTheme {
                PerfilScreen()
            }
        }
    }
}
