package com.example.apptorneosajedrez.ui.fixture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.databinding.FragmentFixtureBinding
import com.example.apptorneosajedrez.model.EstadoTorneo
import com.example.apptorneosajedrez.model.TipoUsuario
import com.example.apptorneosajedrez.model.Torneo
import kotlinx.coroutines.launch

class FixtureFragment : Fragment() {

    private var _binding: FragmentFixtureBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository.getInstance()
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
            authRepository.currentUser.collect { user ->
                val esOrganizador = user?.tipoUsuario == TipoUsuario.ORGANIZADOR
                val esTorneoActivo = torneo?.estado == EstadoTorneo.ACTIVO

                if (esOrganizador && esTorneoActivo) {
                    binding.btnIniciarTorneo.visibility = View.VISIBLE
                } else {
                    binding.btnIniciarTorneo.visibility = View.GONE
                }
            }
        }

        binding.btnIniciarTorneo.setOnClickListener {
            ocultarBotonIniciarTorneo()
            // TODO: llamado a función para ocultar botón "Editar"
            // TODO: llamado a función para auto generar partidas
        }
    }

    private fun ocultarBotonIniciarTorneo() {
        binding.btnIniciarTorneo.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
