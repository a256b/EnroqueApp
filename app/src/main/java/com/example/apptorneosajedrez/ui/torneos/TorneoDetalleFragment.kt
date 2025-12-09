package com.example.apptorneosajedrez.ui.torneos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.databinding.FragmentTorneoDetalleBinding
import com.example.apptorneosajedrez.data.InscripcionRepository
import com.example.apptorneosajedrez.model.Torneo

class TorneoDetalleFragment : Fragment() {

    private var _binding: FragmentTorneoDetalleBinding? = null
    private val binding get() = _binding!!
    private val repoInscripciones = InscripcionRepository()

    private var torneo: Torneo? = null

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
            binding.tvFechaInicio.text = "Fecha de inicio: ${t.fechaInicio}"
            binding.tvFechaFin.text = "Fecha de fin: ${t.fechaFin}"
            binding.tvHoraInicio.text = "Hora de inicio: ${t.horaInicio}"
            binding.tvUbicacion.text = "Lugar: ${t.ubicacion}"
            binding.tvDescripcion.text = t.descripcion

            // Cargar cantidad de inscriptos
            repoInscripciones.escucharInscripciones { inscripciones ->
                val cantidad = inscripciones.count { it.idTorneo == t.idTorneo.toString() }
                binding.tvCantidadInscriptos.text = "Inscriptos: $cantidad"
            }
        }


        binding.btnVerPartidas.setOnClickListener {
            findNavController().navigate(R.id.movimientosFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
