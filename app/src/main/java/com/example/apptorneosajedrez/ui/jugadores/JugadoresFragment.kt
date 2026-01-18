package com.example.apptorneosajedrez.ui.jugadores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.apptorneosajedrez.data.JugadorRepository
import com.example.apptorneosajedrez.databinding.FragmentJugadoresBinding
import com.example.apptorneosajedrez.model.Jugador
import com.google.firebase.firestore.ListenerRegistration

class JugadoresFragment : Fragment() {

    private var _binding: FragmentJugadoresBinding? = null
    private val binding get() = _binding!!

    private val repo = JugadorRepository()
    //private lateinit var adapter: JugadorAdapter
    //private var listaJugadores: List<Jugador> = emptyList()
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
            binding.recyclerViewJugadores.adapter = JugadorAdapter(items)

            //val pendientes = jugadores.filter { it.estado == "pendiente" }
            //val aceptados = jugadores.filter { it.estado == "aceptado" }

           //val items = mutableListOf<JugadorItem>()
            //if (pendientes.isNotEmpty()) {
            //    items.add(JugadorItem.Header("Jugadores pendientes (${pendientes.size})"))
            //    items.addAll(pendientes.map { JugadorItem.JugadorData(it) })
            //}

            //if (aceptados.isNotEmpty()) {
            //    items.add(JugadorItem.Header("Jugadores aceptados (${aceptados.size})"))
            //    items.addAll(aceptados.map { JugadorItem.JugadorData(it) })
            //}

            //adapter = JugadorAdapter(
            //    items,
            //    onAceptarClick = { jugador -> repo.actualizarEstadoJugador(jugador.id, "aceptado") },
            //    onRechazarClick = { jugador -> repo.eliminarJugador(jugador.id) }
            //)
            //binding.recyclerViewJugadores.adapter = adapter
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
        _binding = null
    }
}
