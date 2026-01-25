package com.example.apptorneosajedrez.ui.inscripciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.data.InscripcionRepository
import com.example.apptorneosajedrez.data.JugadorRepository
import com.example.apptorneosajedrez.data.TorneoRepository
import com.example.apptorneosajedrez.databinding.FragmentMisInscripcionesBinding
import com.example.apptorneosajedrez.model.EstadoInscripcion
import com.google.firebase.firestore.ListenerRegistration

class MisInscripcionesFragment : Fragment() {

    private var _binding: FragmentMisInscripcionesBinding? = null
    private val binding get() = _binding!!
    
    private val repoInscripciones = InscripcionRepository()
    private val repoJugadores = JugadorRepository()
    private val repoTorneos = TorneoRepository()
    private val authRepository = AuthRepository.getInstance()

    private var listenerJugadores: ListenerRegistration? = null
    private var listenerTorneos: ListenerRegistration? = null
    private var listenerInscripciones: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMisInscripcionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewMisInscripciones.layoutManager = LinearLayoutManager(requireContext())

        val currentUser = authRepository.getCurrentUserInMemory()
        val currentUserId = currentUser?.uid ?: return

        listenerJugadores = repoJugadores.escucharJugadores { listaJugadores ->
            if (_binding == null) return@escucharJugadores
            val jugadoresMap = listaJugadores.associateBy { it.id }

            listenerTorneos = repoTorneos.escucharTorneos { listaTorneos ->
                if (_binding == null) return@escucharTorneos
                val torneosMap = listaTorneos.associateBy { it.idTorneo }

                listenerInscripciones = repoInscripciones.escucharInscripciones { listaInscripciones ->
                    if (_binding == null) return@escucharInscripciones

                    // Filtra por currentUser
                    val misInscripciones = listaInscripciones.filter { it.idJugador == currentUserId }

                    val inscripcionesInfo = misInscripciones.map { inscripcion ->
                        val jugadorNombre = jugadoresMap[inscripcion.idJugador]?.nombre
                            ?: "Jugador eliminado"

                        val torneoNombre = torneosMap[inscripcion.idTorneo]?.nombre
                            ?: "Torneo eliminado"

                        InscripcionInfo(jugadorNombre, torneoNombre, inscripcion)
                    }

                    val items = mutableListOf<InscripcionItem>()
                    val grupos = inscripcionesInfo.groupBy { it.inscripcion.estado }

                    // Headers + inscripciones
                    grupos[EstadoInscripcion.PENDIENTE]?.let { lista ->
                        if (lista.isNotEmpty()) {
                            items.add(InscripcionItem.Header("Inscripciones pendientes"))
                            items.addAll(lista.map { InscripcionItem.Data(it) })
                        }
                    }

                    grupos[EstadoInscripcion.ACEPTADA]?.let { lista ->
                        if (lista.isNotEmpty()) {
                            items.add(InscripcionItem.Header("Inscripciones aceptadas"))
                            items.addAll(lista.map { InscripcionItem.Data(it) })
                        }
                    }

                    grupos[EstadoInscripcion.RECHAZADA]?.let { lista ->
                        if (lista.isNotEmpty()) {
                            items.add(InscripcionItem.Header("Inscripciones rechazadas"))
                            items.addAll(lista.map { InscripcionItem.Data(it) })
                        }
                    }

                    val adapter = MisInscripcionesAdapter(items)
                    binding.recyclerViewMisInscripciones.adapter = adapter
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerJugadores?.remove()
        listenerTorneos?.remove()
        listenerInscripciones?.remove()
        _binding = null
    }
}
