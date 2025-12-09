package com.example.apptorneosajedrez.ui.nuevos_jugadores

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.apptorneosajedrez.R

class NuevosJugadoresFragment : Fragment() {

    companion object {
        fun newInstance() = NuevosJugadoresFragment()
    }

    private val viewModel: NuevosJugadoresViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_nuevos_jugadores, container, false)
    }
}