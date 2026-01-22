package com.example.apptorneosajedrez.ui.inscripciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.apptorneosajedrez.data.InscripcionRepository
import com.example.apptorneosajedrez.data.JugadorRepository
import com.example.apptorneosajedrez.data.TorneoRepository
import com.example.apptorneosajedrez.databinding.FragmentInscripcionesBinding

class InscripcionesFragment : Fragment() {

    private var _binding: FragmentInscripcionesBinding? = null
    private val binding get() = _binding!!
    private val repoInscripciones = InscripcionRepository()
    private val repoJugadores = JugadorRepository()
    private val repoTorneos = TorneoRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInscripcionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repoJugadores.escucharJugadores { listaJugadores ->
            val jugadoresMap = listaJugadores.associateBy { it.id }

            repoTorneos.escucharTorneos { listaTorneos ->
                val torneosMap = listaTorneos.associateBy { it.idTorneo }

                repoInscripciones.escucharInscripciones { listaInscripciones ->
                    val inscripcionesInfo = listaInscripciones.map { inscripcion ->
                        val jugadorNombre = jugadoresMap[inscripcion.idJugador]?.nombre
                            ?: "Jugador eliminado (id: ${inscripcion.idJugador})"

                        val torneoNombre = torneosMap[inscripcion.idTorneo]?.nombre
                            ?: "Torneo eliminado (id: ${inscripcion.idTorneo})"

                        InscripcionInfo(jugadorNombre, torneoNombre)
                    }

                    val adapter = InscripcionAdapter(inscripcionesInfo)
                    binding.recyclerViewInscripciones.adapter = adapter
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
