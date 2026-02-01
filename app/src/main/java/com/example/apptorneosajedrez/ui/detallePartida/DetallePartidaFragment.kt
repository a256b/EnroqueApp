package com.example.apptorneosajedrez.ui.detallePartida

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.data.JugadorRepository
import com.example.apptorneosajedrez.data.TorneoRepository
import com.example.apptorneosajedrez.databinding.FragmentDetallePartidaBinding
import com.example.apptorneosajedrez.model.EstadoPartida
import com.example.apptorneosajedrez.model.Partida
import com.example.apptorneosajedrez.model.TipoUsuario
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetallePartidaFragment : Fragment() {

    private var _binding: FragmentDetallePartidaBinding? = null
    private val binding get() = _binding!!
    private val jugadorRepository = JugadorRepository()
    private val authRepository = AuthRepository.getInstance()
    private val torneoRepository = TorneoRepository()
    private var partida: Partida? = null
    private var idTorneo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        partida = arguments?.getSerializable("partida") as? Partida
        idTorneo = arguments?.getString("idTorneo")
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

        actualizarUI()

        binding.btnVerPartida.setOnClickListener {
            // TODO: btn para ir a movimientos de partidas
        }

        binding.btnIniciarPartida.setOnClickListener {
            iniciarPartida()
        }
    }

    private fun actualizarUI() {
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

            viewLifecycleOwner.lifecycleScope.launch {
                authRepository.currentUser.collect { user ->
                    val esAdmin = user?.tipoUsuario == TipoUsuario.ORGANIZADOR
                    binding.btnIniciarPartida.visibility = if (esAdmin) View.VISIBLE else View.GONE
                    
                    val jugadoresCargados = !p.idJugador1.isNullOrEmpty() && !p.idJugador2.isNullOrEmpty()
                    val esPendiente = p.estado == EstadoPartida.PENDIENTE
                    
                    binding.btnIniciarPartida.isEnabled = esAdmin && jugadoresCargados && esPendiente
                }
            }
        }
    }

    private fun iniciarPartida() {
        val p = partida ?: return
        val idT = idTorneo ?: return

        val sdfFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val ahora = Date()
        
        val fechaActual = sdfFecha.format(ahora)
        val horaActual = sdfHora.format(ahora)

        binding.btnIniciarPartida.isEnabled = false

        torneoRepository.iniciarPartida(idT, p.idPartida, fechaActual, horaActual) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), "Partida iniciada", Toast.LENGTH_SHORT).show()
                partida = p.copy(
                    estado = EstadoPartida.EN_CURSO,
                    fecha = fechaActual,
                    hora = horaActual
                )
                actualizarUI()
            } else {
                binding.btnIniciarPartida.isEnabled = true
                Toast.makeText(requireContext(), "Error al iniciar partida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
