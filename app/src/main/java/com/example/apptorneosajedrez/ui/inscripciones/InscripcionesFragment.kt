package com.example.apptorneosajedrez.ui.inscripciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apptorneosajedrez.data.InscripcionRepository
import com.example.apptorneosajedrez.data.JugadorRepository
import com.example.apptorneosajedrez.data.TorneoRepository
import com.example.apptorneosajedrez.databinding.FragmentInscripcionesBinding
import com.example.apptorneosajedrez.model.EstadoInscripcion
import com.google.firebase.firestore.ListenerRegistration

class InscripcionesFragment : Fragment() {

    private var _binding: FragmentInscripcionesBinding? = null
    private val binding get() = _binding!!
    
    private val repoInscripciones = InscripcionRepository()
    private val repoJugadores = JugadorRepository()
    private val repoTorneos = TorneoRepository()

    private var listenerJugadores: ListenerRegistration? = null
    private var listenerTorneos: ListenerRegistration? = null
    private var listenerInscripciones: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInscripcionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewInscripciones.layoutManager = LinearLayoutManager(requireContext())

        listenerJugadores = repoJugadores.escucharJugadores { listaJugadores ->
            _binding ?: return@escucharJugadores
            val jugadoresMap = listaJugadores.associateBy { it.id }

            listenerTorneos = repoTorneos.escucharTorneos { listaTorneos ->
                val torneosMap = listaTorneos.associateBy { it.idTorneo }

                listenerInscripciones = repoInscripciones.escucharInscripciones { listaInscripciones ->
                    if (_binding == null) return@escucharInscripciones

                    val inscripcionesInfo = listaInscripciones.map { inscripcion ->
                        val jugadorNombre = jugadoresMap[inscripcion.idJugador]?.nombre
                            ?: "Jugador eliminado (id: ${inscripcion.idJugador})"

                        val torneoNombre = torneosMap[inscripcion.idTorneo]?.nombre
                            ?: "Torneo eliminado (id: ${inscripcion.idTorneo})"

                        InscripcionInfo(jugadorNombre, torneoNombre, inscripcion)
                    }

                    // Crea headers y agrupa los items
                    val items = mutableListOf<InscripcionItem>()
                    val grupos = inscripcionesInfo.groupBy { it.inscripcion.estado }

                    // Inscripciones pendientes
                    grupos[EstadoInscripcion.PENDIENTE]?.let { lista ->
                        if (lista.isNotEmpty()) {
                            items.add(InscripcionItem.Header("Inscripciones pendientes"))
                            items.addAll(lista.map { InscripcionItem.Data(it) })
                        }
                    }

                    // Inscripciones aceptadas
                    grupos[EstadoInscripcion.ACEPTADA]?.let { lista ->
                        if (lista.isNotEmpty()) {
                            items.add(InscripcionItem.Header("Inscripciones aceptadas"))
                            items.addAll(lista.map { InscripcionItem.Data(it) })
                        }
                    }

                    // Inscripciones rechazadas
                    grupos[EstadoInscripcion.RECHAZADA]?.let { lista ->
                        if (lista.isNotEmpty()) {
                            items.add(InscripcionItem.Header("Inscripciones rechazadas"))
                            items.addAll(lista.map { InscripcionItem.Data(it) })
                        }
                    }

                    val adapter = InscripcionAdapter(items, object : InscripcionAdapter.OnInscripcionDecisionListener {
                        override fun onAceptar(info: InscripcionInfo) {
                            val idTorneo = info.inscripcion.idTorneo
                            val idJugador = info.inscripcion.idJugador
                            
                            repoInscripciones.actualizarEstadoInscripcion(info.inscripcion.id, EstadoInscripcion.ACEPTADA)
                            
                            repoTorneos.agregarJugadorATorneo(idTorneo, idJugador) { exito ->
                                if (exito) {
                                    Toast.makeText(requireContext(), "Jugador vinculado al torneo", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), "Error al vincular jugador", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onRechazar(info: InscripcionInfo) {
                            repoInscripciones.actualizarEstadoInscripcion(info.inscripcion.id, EstadoInscripcion.RECHAZADA)
                        }
                    })
                    binding.recyclerViewInscripciones.adapter = adapter
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
