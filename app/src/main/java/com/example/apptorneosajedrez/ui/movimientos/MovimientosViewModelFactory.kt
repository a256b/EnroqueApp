import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.data.MovimientosRepository
import com.example.apptorneosajedrez.data.PartidasRepository
import com.example.apptorneosajedrez.data.TorneoRepository
import com.example.apptorneosajedrez.ui.movimientos.MovimientosViewModel

class MovimientosViewModelFactory(
    private val torneoId: String,
    private val partidaId: String,
    private val repository: MovimientosRepository,
    private val authRepository: AuthRepository,
    private val torneoRepository: TorneoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovimientosViewModel::class.java)) {
            return MovimientosViewModel(
                torneoId = torneoId,
                partidaId = partidaId,
                movimientosRepository = repository,
                authRepository = authRepository,
                torneoRepository = torneoRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
