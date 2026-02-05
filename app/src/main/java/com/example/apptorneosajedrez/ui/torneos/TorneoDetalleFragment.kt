package com.example.apptorneosajedrez.ui.torneos

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.databinding.FragmentDetalleTorneoBinding
import com.example.apptorneosajedrez.data.InscripcionRepository
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.data.TorneoRepository
import com.example.apptorneosajedrez.model.Torneo
import com.example.apptorneosajedrez.model.EstadoTorneo
import com.example.apptorneosajedrez.model.EstadoComoJugador
import com.example.apptorneosajedrez.model.Inscripcion
import com.example.apptorneosajedrez.model.EstadoInscripcion
import com.example.apptorneosajedrez.model.TipoUsuario
import com.example.apptorneosajedrez.ui.fixture.FixtureViewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class TorneoDetalleFragment : Fragment() {

    private var _binding: FragmentDetalleTorneoBinding? = null
    private val binding get() = _binding!!
    private val repoInscripciones = InscripcionRepository()
    private val repoTorneos = TorneoRepository()
    private val authRepository = AuthRepository.getInstance()
    private val fixtureViewModel: FixtureViewModel by activityViewModels()

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
        _binding = FragmentDetalleTorneoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        torneo?.let { t ->
            actualizarVista(t)

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
                    
                    val idTorneoActual = t.idTorneo
                    fixtureViewModel.torneosConEditarOculto.collect { setOcultos ->
                        val estaOcultoParaEsteTorneo = setOcultos.contains(idTorneoActual)
                        
                        binding.btnEditarTorneo.visibility = if (user?.tipoUsuario == TipoUsuario.ORGANIZADOR && !estaOcultoParaEsteTorneo) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
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
            
            binding.btnEditarTorneo.setOnClickListener {
                mostrarDialogEditar(t)
            }

            binding.btnVerPartidas.setOnClickListener {
                val bundle = Bundle().apply {
                    putSerializable("torneo", t)
                }
                findNavController().navigate(R.id.nav_fixtureFragment, bundle)
            }
        }
    }

    private fun actualizarVista(t: Torneo) {
        binding.nombreTorneo.text = t.nombre
        binding.tvEstadoTorneo.text = t.estado.name
        binding.tvFechaInicio.text = "Inicio: ${t.fechaInicio}"
        binding.tvFechaFin.text = "Finalización: ${t.fechaFin}"
        binding.tvHoraInicio.text = "${t.horaInicio}hs."
        binding.tvUbicacion.text = "Lugar: ${t.ubicacion}"
        binding.tvDescripcion.text = t.descripcion

        binding.tvEstadoTorneo.backgroundTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    colorFondoSegunEstado(t)
                )
            )
    }

    @ColorRes
    private fun colorFondoSegunEstado(torneo: Torneo): Int {
        return when (torneo.estado) {
            EstadoTorneo.ACTIVO -> R.color.estado_torneo_activo
            EstadoTorneo.FINALIZADO -> R.color.estado_torneo_finalizado
            EstadoTorneo.SUSPENDIDO -> R.color.estado_torneo_suspendido
            EstadoTorneo.PROXIMO -> R.color.estado_torneo_proximo
        }
    }

    private fun mostrarDialogEditar(t: Torneo) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_torneo, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner_estado_torneo)

        val estados = EstadoTorneo.values()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, estados)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        
        // Seleccionar el estado actual
        spinner.setSelection(estados.indexOf(t.estado))

        builder.setView(dialogView)
        builder.setPositiveButton("Guardar") { _, _ ->
            val nuevoEstado = spinner.selectedItem as EstadoTorneo
            if (nuevoEstado != t.estado) {
                repoTorneos.actualizarEstadoTorneo(t.idTorneo, nuevoEstado) { exito ->
                    if (exito) {
                        Toast.makeText(requireContext(), "Estado actualizado", Toast.LENGTH_SHORT).show()
                        // Actualizamos el objeto local y la vista
                        torneo = t.copy(estado = nuevoEstado)
                        torneo?.let { actualizarVista(it) }
                    } else {
                        Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerInscripciones?.remove()
        _binding = null
    }
}
