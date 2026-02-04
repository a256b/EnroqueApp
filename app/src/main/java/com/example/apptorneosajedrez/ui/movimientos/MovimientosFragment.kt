package com.example.apptorneosajedrez.ui.movimientos

import MovimientosViewModelFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.data.MovimientosRepository
import com.example.apptorneosajedrez.databinding.FragmentMovimientosBinding
import com.example.apptorneosajedrez.ui._theme.AppTorneosTheme
import com.google.firebase.firestore.FirebaseFirestore

class MovimientosFragment : Fragment() {

    private var _binding: FragmentMovimientosBinding? = null
    private val binding get() = _binding!!

    // TODO: Constantes para pruebas
    companion object {
        const val TORNEO_ID_DEBUG = "0VSJmWIcqLUtNs9nyLUt"
        const val PARTIDA_ID_DEBUG = "YeOkCTXRETTaYLPNmg6E"
    }

    // Repositorios
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val movimientosRepository by lazy { MovimientosRepository(firestore) }

    // 👇 USAR el singleton correcto de AuthRepository
    private val authRepository by lazy { AuthRepository.getInstance() }

    private val viewModel: MovimientosViewModel by viewModels {
        MovimientosViewModelFactory(
            torneoId = TORNEO_ID_DEBUG,
            partidaId = PARTIDA_ID_DEBUG,
            repository = movimientosRepository,
            authRepository = authRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovimientosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewMovimientos.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                AppTorneosTheme {
                    val uiState by viewModel.uiState.observeAsState(
                        initial = MovimientosUiState()
                    )

                    MovimientosScreen(
                        uiState = uiState,
                        onBorrarTextoClick = viewModel::onBorrarTextoClick,
                        onDeshacerMovimientoClick = viewModel::onDeshacerMovimientoClick,
                        onEnviarMovimientoClick = { viewModel.onEnviarMovimientoClick() },
                        onMovimientoChange = viewModel::onMovimientoChange
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
