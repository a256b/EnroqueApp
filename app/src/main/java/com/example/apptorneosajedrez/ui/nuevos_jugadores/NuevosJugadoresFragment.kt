package com.example.apptorneosajedrez.ui.nuevos_jugadores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apptorneosajedrez.databinding.FragmentNuevosJugadoresBinding
import com.example.apptorneosajedrez.model.EstadoComoJugador
import com.example.apptorneosajedrez.model.Usuario

class NuevosJugadoresFragment : Fragment() {

    private lateinit var binding: FragmentNuevosJugadoresBinding
    private val viewModel: NuevosJugadoresViewModel by viewModels()
    private lateinit var adapter: NuevosJugadoresAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNuevosJugadoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Adapter con callbacks
        // TODO: mejorar el rendimiento usando el user cacheado
        
        adapter = NuevosJugadoresAdapter(
            emptyList(),
            object : NuevosJugadoresAdapter.OnDecisionListener {

                override fun onAceptar(usuario: Usuario) {
                    viewModel.actualizarEstado(usuario, EstadoComoJugador.ACEPTADO)
                    mostrarMensaje("${usuario.nombreCompleto} aceptado")
                }

                override fun onRechazar(usuario: Usuario) {
                    viewModel.actualizarEstado(usuario, EstadoComoJugador.RECHAZADO)
                    mostrarMensaje("${usuario.nombreCompleto} rechazado")
                }
            }
        )

        binding.recyclerNuevosJugadores.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerNuevosJugadores.adapter = adapter

        // Observa los usuarios pendientes
        viewModel.usuariosPendientes.observe(viewLifecycleOwner) { lista ->
            adapter.actualizarLista(lista)
        }

        viewModel.cargarPendientes()
    }

    private fun mostrarMensaje(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
