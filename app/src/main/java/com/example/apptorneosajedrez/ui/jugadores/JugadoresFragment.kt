package com.example.apptorneosajedrez.ui.jugadores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.data.JugadorRepository
import com.example.apptorneosajedrez.databinding.FragmentJugadoresBinding
import com.google.firebase.firestore.ListenerRegistration

class JugadoresFragment : Fragment() {

    private var _binding: FragmentJugadoresBinding? = null
    private val binding get() = _binding!!

    private val repo = JugadorRepository()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJugadoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listenerRegistration = repo.escucharJugadores { jugadores ->
            val items = jugadores.map { JugadorItem.JugadorData(it) }

            binding.recyclerViewJugadores.adapter = JugadorAdapter(items) { jugador ->
                val jugadorId = jugador.id

                val bundle = Bundle().apply {
                    putString("jugadorId", jugadorId)
                }

                findNavController().navigate(
                    R.id.action_nav_jugadores_to_detalleJugadorFragment,
                    bundle
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
        _binding = null
    }
}
