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

        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                authRepository.currentUser,
                fixtureViewModel.debeOcultarIniciarTorneo
            ) { user, ocultar ->
                val esOrganizador = user?.tipoUsuario == TipoUsuario.ORGANIZADOR
                val esTorneoActivo = torneo?.estado == EstadoTorneo.ACTIVO
                
                esOrganizador && esTorneoActivo && !ocultar
            }.collect { visible ->
                binding.btnIniciarTorneo.visibility = if (visible) View.VISIBLE else View.GONE
            }
        }

        binding.btnIniciarTorneo.setOnClickListener {
            ocultarBotonIniciarTorneo()
            ocultarBotonEditarDetalle()
            crearPartidas()
        }
    }

    private fun ocultarBotonIniciarTorneo() {
        fixtureViewModel.ocultarBotonIniciarTorneo()
    }

    private fun ocultarBotonEditarDetalle() {
        fixtureViewModel.ocultarBotonEditar()
    }

    private fun crearPartidas() {
        val idTorneo = torneo?.idTorneo
        if (idTorneo != null) {
            torneoRepository.autoGenerarPartidas(idTorneo) { exito ->
                if (exito) {
                    Toast.makeText(requireContext(), "Partidas generadas correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error al generar partidas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
