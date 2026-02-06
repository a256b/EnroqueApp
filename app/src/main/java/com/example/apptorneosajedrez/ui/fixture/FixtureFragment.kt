package com.example.apptorneosajedrez.ui.fixture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.data.TorneoRepository
import com.example.apptorneosajedrez.databinding.FragmentFixtureBinding
import com.example.apptorneosajedrez.model.EstadoTorneo
import com.example.apptorneosajedrez.model.Fase
import com.example.apptorneosajedrez.model.Partida
import com.example.apptorneosajedrez.model.TipoUsuario
import com.example.apptorneosajedrez.model.Torneo
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FixtureFragment : Fragment() {

    private var _binding: FragmentFixtureBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository.getInstance()
    private val torneoRepository = TorneoRepository()
    private val fixtureViewModel: FixtureViewModel by activityViewModels()
    private var torneo: Torneo? = null
    private var partidasGeneradas: List<Partida> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        torneo = arguments?.getSerializable("torneo") as? Torneo
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFixtureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idTorneoActual = torneo?.idTorneo ?: ""

        if (idTorneoActual.isNotEmpty()) {
            cargarPartidas(idTorneoActual)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                authRepository.currentUser,
                fixtureViewModel.torneosConIniciarOculto
            ) { user, setOcultos ->
                val esOrganizador = user?.tipoUsuario == TipoUsuario.ORGANIZADOR
                val esTorneoActivo = torneo?.estado == EstadoTorneo.ACTIVO
                val estaOcultoParaEsteTorneo = setOcultos.contains(idTorneoActual)
                
                esOrganizador && esTorneoActivo && !estaOcultoParaEsteTorneo
            }.collect { visible ->
                binding.btnIniciarTorneo.visibility = if (visible) View.VISIBLE else View.GONE
            }
        }

        binding.btnIniciarTorneo.setOnClickListener {
            iniciarTorneoConValidacion()
        }

        configurarClickCards()
    }

    private fun actualizarAparienciaCards() {
        val cardsPorFase = mapOf(
            Fase.CUARTOS to listOf(binding.m1, binding.m2, binding.m3, binding.m4),
            Fase.SEMI to listOf(binding.semi1, binding.semi2),
            Fase.FINAL to listOf(binding.finalMatch)
        )

        cardsPorFase.forEach { (fase, cards) ->
            val cantidadPartidasEnFase = partidasGeneradas.count { it.fase == fase }
            cards.forEachIndexed { index, card ->
                card.isEnabled = index < cantidadPartidasEnFase
            }
        }
    }

    private fun cargarPartidas(idTorneo: String) {
        torneoRepository.obtenerPartidas(idTorneo) { partidas ->
            partidasGeneradas = partidas
            if (partidas.isNotEmpty()) {
                fixtureViewModel.ocultarBotonIniciarTorneo(idTorneo)
                fixtureViewModel.ocultarBotonEditar(idTorneo)
            }
            actualizarAparienciaCards()
        }
    }

    private fun configurarClickCards() {
        binding.finalMatch.setOnClickListener { navegarADetallePartida(Fase.FINAL, 0) }
        binding.semi1.setOnClickListener { navegarADetallePartida(Fase.SEMI, 0) }
        binding.semi2.setOnClickListener { navegarADetallePartida(Fase.SEMI, 1) }
        binding.m1.setOnClickListener { navegarADetallePartida(Fase.CUARTOS, 0) }
        binding.m2.setOnClickListener { navegarADetallePartida(Fase.CUARTOS, 1) }
        binding.m3.setOnClickListener { navegarADetallePartida(Fase.CUARTOS, 2) }
        binding.m4.setOnClickListener { navegarADetallePartida(Fase.CUARTOS, 3) }
    }

    private fun navegarADetallePartida(fase: Fase, indiceEnFase: Int) {
        val partidasDeFase = partidasGeneradas.filter { it.fase == fase }

        // Check si existe la partida solicitada
        if (indiceEnFase < partidasDeFase.size) {
            val partida = partidasDeFase[indiceEnFase]
            val bundle = Bundle().apply {
                putSerializable("partida", partida)
                putString("idTorneo", torneo?.idTorneo)
            }
            findNavController().navigate(R.id.action_fixtureFragment_to_detallePartidaFragment, bundle)
        } else {
            //TODO: Si no existe ocultar la CardView...
        }
    }

    private fun iniciarTorneoConValidacion() {
        val idTorneo = torneo?.idTorneo
        if (idTorneo != null) {
            torneoRepository.autoGenerarPartidas(idTorneo) { exito ->
                if (exito) {
                    ocultarBotonIniciarTorneo(idTorneo)
                    ocultarBotonEditarDetalle(idTorneo)
                    cargarPartidas(idTorneo) // Test: recarga las partidas y habilita clicks
                    Toast.makeText(requireContext(), "Torneo iniciado: Partidas generadas", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error: El torneo debe tener entre 2 y 8 jugadores.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun ocultarBotonIniciarTorneo(idTorneo: String) {
        fixtureViewModel.ocultarBotonIniciarTorneo(idTorneo)
    }

    private fun ocultarBotonEditarDetalle(idTorneo: String) {
        fixtureViewModel.ocultarBotonEditar(idTorneo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
