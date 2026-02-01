package com.example.apptorneosajedrez.ui.fixture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.data.TorneoRepository
import com.example.apptorneosajedrez.databinding.FragmentFixtureBinding
import com.example.apptorneosajedrez.model.EstadoTorneo
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
    }

    private fun iniciarTorneoConValidacion() {
        val idTorneo = torneo?.idTorneo
        if (idTorneo != null) {
            torneoRepository.autoGenerarPartidas(idTorneo) { exito ->
                if (exito) {
                    ocultarBotonIniciarTorneo(idTorneo)
                    ocultarBotonEditarDetalle(idTorneo)
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
