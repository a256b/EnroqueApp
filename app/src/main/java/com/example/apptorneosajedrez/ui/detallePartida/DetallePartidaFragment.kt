package com.example.apptorneosajedrez.ui.detallePartida

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
            val action =
                DetallePartidaFragmentDirections.actionDetallePartidaFragmentToMovimientosFragment(
                    torneoId = idTorneo ?: "",
                    partidaId = partida?.idPartida ?: ""
                )

            findNavController().navigate(action)
        }

        binding.btnIniciarPartida.setOnClickListener {
            iniciarPartida()
        }

        binding.btnFinalizarPartida.setOnClickListener {
            mostrarDialogGanador()
        }
    }

    private fun actualizarUI() {
        val binding = _binding ?: return
        val p = partida ?: return

        binding.tvFase.text = p.fase?.name ?: "---"
        binding.tvEstado.text = p.estado.name

        // Ganador
        if (!p.ganador.isNullOrEmpty()) {
            jugadorRepository.obtenerJugador(p.ganador) { ganadorObj ->
                val b = _binding ?: return@obtenerJugador
                b.tvGanador.text = "Ganador: ${ganadorObj?.nombre ?: p.ganador}"
            }
        } else {
            binding.tvGanador.text = "Ganador: ---"
        }

        binding.tvFecha.text = "Fecha: ${if (p.fecha.isNullOrEmpty()) "---" else p.fecha}"
        binding.tvHora.text = "Hora: ${if (p.hora.isNullOrEmpty()) "---" else p.hora}"

        // Jugador 1
        p.idJugador1?.let { id ->
            jugadorRepository.obtenerJugador(id) { jugador ->
                val b = _binding ?: return@obtenerJugador
                b.tvJugador1.text = "${jugador?.nombre ?: "Sin nombre"} - Blancas"
            }
        } ?: run { binding.tvJugador1.text = "Pendiente - Blancas" }

        // Jugador 2
        p.idJugador2?.let { id ->
            jugadorRepository.obtenerJugador(id) { jugador ->
                val b = _binding ?: return@obtenerJugador
                b.tvJugador2.text = "${jugador?.nombre ?: "Sin nombre"} - Negras"
            }
        } ?: run { binding.tvJugador2.text = "Pendiente - Negras" }

        viewLifecycleOwner.lifecycleScope.launch {
            authRepository.currentUser.collect { user ->
                val b = _binding ?: return@collect
                val esAdmin = user?.tipoUsuario == TipoUsuario.ORGANIZADOR

                val esPendiente = p.estado == EstadoPartida.PENDIENTE
                val esEnCurso = p.estado == EstadoPartida.EN_CURSO
                val jugadoresCargados =
                    !p.idJugador1.isNullOrEmpty() && !p.idJugador2.isNullOrEmpty()

                b.btnIniciarPartida.visibility =
                    if (esAdmin && esPendiente) View.VISIBLE else View.GONE
                b.btnIniciarPartida.isEnabled = jugadoresCargados

                b.btnFinalizarPartida.visibility =
                    if (esAdmin && esEnCurso) View.VISIBLE else View.GONE
            }
        }
    }

    private fun iniciarPartida() {
        val binding = _binding ?: return
        val p = partida ?: return
        val idT = idTorneo ?: return

        val sdfFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val ahora = Date()

        val fechaActual = sdfFecha.format(ahora)
        val horaActual = sdfHora.format(ahora)

        binding.btnIniciarPartida.isEnabled = false

        torneoRepository.iniciarPartida(idT, p.idPartida, fechaActual, horaActual) { exito ->
            val b = _binding ?: return@iniciarPartida
            if (exito) {
                Toast.makeText(requireContext(), "Partida iniciada", Toast.LENGTH_SHORT).show()
                partida = p.copy(
                    estado = EstadoPartida.EN_CURSO,
                    fecha = fechaActual,
                    hora = horaActual
                )
                actualizarUI()
            } else {
                b.btnIniciarPartida.isEnabled = true
                Toast.makeText(requireContext(), "Error al iniciar partida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogGanador() {
        val p = partida ?: return
        val idJ1 = p.idJugador1 ?: return
        val idJ2 = p.idJugador2 ?: return

        // Obtener nombres para el dialog
        jugadorRepository.obtenerJugador(idJ1) { j1 ->
            jugadorRepository.obtenerJugador(idJ2) { j2 ->
                // Si el fragment ya no está añadido, no mostramos nada
                if (!isAdded) return@obtenerJugador

                val nombres = arrayOf(j1?.nombre ?: "Jugador 1", j2?.nombre ?: "Jugador 2")
                val ids = arrayOf(idJ1, idJ2)
                var seleccionado = 0

                AlertDialog.Builder(requireContext())
                    .setTitle("Seleccionar Ganador")
                    .setSingleChoiceItems(nombres, 0) { _, which ->
                        seleccionado = which
                    }
                    .setPositiveButton("GUARDAR") { _, _ ->
                        finalizarPartida(ids[seleccionado])
                    }
                    .setNegativeButton("CANCELAR", null)
                    .show()
            }
        }
    }

    private fun finalizarPartida(idGanador: String) {
        val binding = _binding ?: return
        val p = partida ?: return
        val idT = idTorneo ?: return

        binding.btnFinalizarPartida.isEnabled = false

        torneoRepository.finalizarPartida(idT, p, idGanador) { exito ->
            val b = _binding ?: return@finalizarPartida
            if (exito) {
                Toast.makeText(requireContext(), "Partida finalizada", Toast.LENGTH_SHORT).show()
                partida = p.copy(
                    estado = EstadoPartida.FINALIZADA,
                    ganador = idGanador
                )
                actualizarUI()
            } else {
                b.btnFinalizarPartida.isEnabled = true
                Toast.makeText(requireContext(), "Error al finalizar partida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
