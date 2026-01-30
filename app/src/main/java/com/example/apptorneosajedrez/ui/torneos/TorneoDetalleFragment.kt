package com.example.apptorneosajedrez.ui.torneos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.databinding.FragmentTorneoDetalleBinding
import com.example.apptorneosajedrez.data.InscripcionRepository
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.model.Torneo
import com.example.apptorneosajedrez.model.EstadoTorneo
import com.example.apptorneosajedrez.model.EstadoComoJugador
import com.example.apptorneosajedrez.model.Inscripcion
import com.example.apptorneosajedrez.model.EstadoInscripcion
import com.example.apptorneosajedrez.model.TipoUsuario
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class TorneoDetalleFragment : Fragment() {

    private var _binding: FragmentTorneoDetalleBinding? = null
    private val binding get() = _binding!!
    private val repoInscripciones = InscripcionRepository()
    private val authRepository = AuthRepository.getInstance()

    private var torneo: Torneo? = null
    private var currentUserId: String? = null
    private var listenerInscripciones: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        torneo = arguments?.getSerializable("torneo") as? Torneo
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTorneoDetalleBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        torneo?.let { t ->
            binding.nombreTorneo.text = t.nombre
            binding.tvEstadoTorneo.text = t.estado.name
            binding.tvFechaInicio.text = "Fecha de inicio: ${t.fechaInicio}"
            binding.tvFechaFin.text = "Fecha de fin: ${t.fechaFin}"
            binding.tvHoraInicio.text = "Hora de inicio: ${t.horaInicio}"
            binding.tvUbicacion.text = "Lugar: ${t.ubicacion}"
            binding.tvDescripcion.text = t.descripcion

            // Control de restricciones
            var esJugadorAceptado = false
            var esJugadorSinAlta = false
            var hayCapacidad = true
            var yaInscripto = false
            val esTorneoProximo = t.estado == EstadoTorneo.PROXIMO

            fun actualizarInterfazFiltros() {
                val b = _binding ?: return
                
                // Habilitar botón si cumple todo y NO está inscripto de forma activa
                b.btnInscribirse.isEnabled = esTorneoProximo && esJugadorAceptado && hayCapacidad && !yaInscripto
                
                if (yaInscripto) {
                    b.btnInscribirse.text = "Ya inscripto"
                } else {
                    b.btnInscribirse.text = "Inscribirse"
                }

                // Cumple lo anterior, pero el usuario no es jugador
                if (esTorneoProximo && hayCapacidad && esJugadorSinAlta && !yaInscripto) {
                    b.tvMensajeJugador.visibility = View.VISIBLE
                } else {
                    b.tvMensajeJugador.visibility = View.GONE
                }
            }

            // Current user
            viewLifecycleOwner.lifecycleScope.launch {
                authRepository.currentUser.collect { user ->
                    currentUserId = user?.uid
                    esJugadorAceptado = user?.estadoComoJugador == EstadoComoJugador.ACEPTADO
                    esJugadorSinAlta = user?.estadoComoJugador == EstadoComoJugador.NINGUNO || 
                                       user?.estadoComoJugador == EstadoComoJugador.RECHAZADO
                    
                    binding.btnEditarTorneo.visibility = if (user?.tipoUsuario == TipoUsuario.ORGANIZADOR) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    
                    actualizarInterfazFiltros()
                }
            }

            // Verifica inscripciones
            listenerInscripciones = repoInscripciones.escucharInscripciones { inscripciones ->
                if (_binding == null) return@escucharInscripciones

                val inscripcionesDeEsteTorneo = inscripciones.filter { it.idTorneo == t.idTorneo.toString() }
                
                // Se suman al contador si el estado es ACEPTADA o PENDIENTE
                val cantidadValida = inscripcionesDeEsteTorneo.count { it.estado != EstadoInscripcion.RECHAZADA }
                binding.tvCantidadInscriptos.text = "Inscriptos: $cantidadValida"
                
                // La capacidad se mide por las solicitudes no rechazadas
                hayCapacidad = cantidadValida < 8
                
                // Verificamos si ya existe una inscripción activa/pendiente del usuario
                yaInscripto = inscripcionesDeEsteTorneo.any { 
                    it.idJugador == currentUserId && it.estado != EstadoInscripcion.RECHAZADA 
                }
                
                actualizarInterfazFiltros()
            }

            binding.btnInscribirse.setOnClickListener {
                val uid = currentUserId
                if (uid != null) {
                    val nuevaInscripcion = Inscripcion(
                        idJugador = uid,
                        idTorneo = t.idTorneo.toString(),
                        estado = EstadoInscripcion.PENDIENTE
                    )
                    
                    binding.btnInscribirse.isEnabled = false // Evitar clics múltiples
                    
                    repoInscripciones.agregarInscripcion(nuevaInscripcion) { exito ->
                        if (_binding == null) return@agregarInscripcion
                        
                        if (exito) {
                            Toast.makeText(requireContext(), "Inscripción solicitada, esperando respuesta", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Error al solicitar inscripción", Toast.LENGTH_SHORT).show()
                            actualizarInterfazFiltros()
                        }
                    }
                }
            }
        }

        binding.btnVerPartidas.setOnClickListener {
            findNavController().navigate(R.id.nav_fixtureFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerInscripciones?.remove()
        _binding = null
    }
}
