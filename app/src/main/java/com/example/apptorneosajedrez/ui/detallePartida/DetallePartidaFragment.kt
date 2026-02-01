package com.example.apptorneosajedrez.ui.detallePartida

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.apptorneosajedrez.data.JugadorRepository
import com.example.apptorneosajedrez.databinding.FragmentDetallePartidaBinding
import com.example.apptorneosajedrez.model.Partida

class DetallePartidaFragment : Fragment() {

    private var _binding: FragmentDetallePartidaBinding? = null
    private val binding get() = _binding!!
    private val jugadorRepository = JugadorRepository()
    private var partida: Partida? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        partida = arguments?.getSerializable("partida") as? Partida
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetallePartidaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        partida?.let { p ->
            binding.tvFase.text = p.fase?.name ?: "---"
            binding.tvEstado.text = p.estado.name
            binding.tvGanador.text = "Ganador: ${if (p.ganador.isNullOrEmpty()) "---" else p.ganador}"
            binding.tvFecha.text = "Fecha: ${if (p.fecha.isNullOrEmpty()) "---" else p.fecha}"
            binding.tvHora.text = "Hora: ${if (p.hora.isNullOrEmpty()) "---" else p.hora}"

            // Cargar nombres de jugadores
            p.idJugador1?.let { id ->
                jugadorRepository.obtenerJugador(id) { jugador ->
                    binding.tvJugador1.text = "${jugador?.nombre ?: "Sin nombre"} - Blancas"
                }
            } ?: run { binding.tvJugador1.text = "Pendiente - Blancas" }

            p.idJugador2?.let { id ->
                jugadorRepository.obtenerJugador(id) { jugador ->
                    binding.tvJugador2.text = "${jugador?.nombre ?: "Sin nombre"} - Negras"
                }
            } ?: run { binding.tvJugador2.text = "Pendiente - Negras" }
        }

        binding.btnVerPartida.setOnClickListener {
            // TODO: btn para ir a movimientos de partidas
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
