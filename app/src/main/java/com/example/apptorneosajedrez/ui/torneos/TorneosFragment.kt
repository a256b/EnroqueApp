package com.example.apptorneosajedrez.ui.torneos

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.data.MarcadorRepository
import com.example.apptorneosajedrez.data.TorneoRepository
import com.example.apptorneosajedrez.databinding.FragmentTorneosBinding
import com.example.apptorneosajedrez.model.EstadoTorneo
import com.example.apptorneosajedrez.model.TipoUsuario
import com.example.apptorneosajedrez.model.Torneo
import kotlinx.coroutines.launch
import java.util.Calendar

class TorneosFragment : Fragment() {

    private var _binding: FragmentTorneosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TorneoViewModel by viewModels()
    private val authRepository = AuthRepository.getInstance()
    private var torneosList: List<Torneo> = emptyList()

    // Variables para mantener los filtros aplicados
    private var filtroNombre = ""
    private var filtroUbicacion = "Todas las ubicaciones"
    private var filtroEstado = "Todos los estados"
    private var filtroDescripcion = ""
    private var filtroFecha = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTorneosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewLifecycleOwner.lifecycleScope.launch {
            authRepository.currentUser.collect { user ->
                binding.fabAgregarTorneo.visibility = 
                    if (user?.tipoUsuario == TipoUsuario.ORGANIZADOR) View.VISIBLE else View.GONE
            }
        }

        observarTorneos()
        binding.fabAgregarTorneo.setOnClickListener {
            mostrarDialogoAgregarTorneo()
        }
        binding.fabFiltrarTorneo.setOnClickListener {
            mostrarDialogoFiltro()
        }
    }

    private fun observarTorneos() {
        viewModel.torneos.observe(viewLifecycleOwner) { lista ->
            torneosList = lista
            aplicarFiltros()
        }
    }

    private fun aplicarFiltros() {
        val filtrados = torneosList.filter { torneo ->
            val coincideNombre = filtroNombre.isEmpty() || torneo.nombre.contains(filtroNombre, ignoreCase = true)
            val coincideUbicacion = filtroUbicacion == "Todas las ubicaciones" || torneo.ubicacion == filtroUbicacion
            val coincideEstado = filtroEstado == "Todos los estados" || torneo.estado.name.equals(filtroEstado, ignoreCase = true)
            val coincideDescripcion = filtroDescripcion.isEmpty() || torneo.descripcion.contains(filtroDescripcion, ignoreCase = true)
            val coincideFecha = filtroFecha.isEmpty() || torneo.fechaInicio == filtroFecha

            coincideNombre && coincideUbicacion && coincideEstado && coincideDescripcion && coincideFecha
        }
        setupRecyclerView(filtrados)
    }

    private fun setupRecyclerView(listaMostrar: List<Torneo>) {
        val activos = listaMostrar.filter { it.estado == EstadoTorneo.ACTIVO }
        val proximos = listaMostrar.filter { it.estado == EstadoTorneo.PROXIMO }
        val finalizados = listaMostrar.filter { it.estado == EstadoTorneo.FINALIZADO }
        val suspendidos = listaMostrar.filter { it.estado == EstadoTorneo.SUSPENDIDO }

        val items = mutableListOf<TorneoItem>()

        if (activos.isNotEmpty()) {
            items.add(TorneoItem.Header("Torneos activos"))
            activos.forEach { items.add(TorneoItem.TorneoData(it)) }
        }

        if (proximos.isNotEmpty()) {
            items.add(TorneoItem.Header("Próximos torneos"))
            proximos.forEach { items.add(TorneoItem.TorneoData(it)) }
        }

        if (finalizados.isNotEmpty()) {
            items.add(TorneoItem.Header("Torneos finalizados"))
            finalizados.forEach { items.add(TorneoItem.TorneoData(it)) }
        }

        if (suspendidos.isNotEmpty()) {
            items.add(TorneoItem.Header("Torneos suspendidos"))
            suspendidos.forEach { items.add(TorneoItem.TorneoData(it)) }
        }

        binding.recyclerViewTorneos.adapter =
            TorneoAdapter(items, requireContext()) { torneo ->
                val bundle = Bundle().apply { putSerializable("torneo", torneo) }
                findNavController().navigate(
                    R.id.action_nav_torneos_to_detalleTorneoFragment,
                    bundle
                )
            }
    }

    private fun mostrarDialogoFiltro() {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filtro_torneo, null)

        val etNombre = view.findViewById<EditText>(R.id.etNombreFiltro)
        val spinnerUbicacion = view.findViewById<Spinner>(R.id.spinnerUbicacionFiltro)
        val spinnerEstado = view.findViewById<Spinner>(R.id.spinnerEstadoFiltro)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcionFiltro)
        val etFechaInicio = view.findViewById<EditText>(R.id.etFechaInicioFiltro)
        val btnLimpiar = view.findViewById<Button>(R.id.btnLimpiarFiltro)

        // Configurar Spinner Ubicación
        MarcadorRepository().escucharMarcadores { lista ->
            val ubicaciones = listOf("Todas las ubicaciones") + lista.filter { it.categoria.name == "TORNEO" }.map { it.nombre }
            val adapterUbi = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ubicaciones)
            adapterUbi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerUbicacion.adapter = adapterUbi
            
            val indexUbi = ubicaciones.indexOf(filtroUbicacion)
            if (indexUbi >= 0) spinnerUbicacion.setSelection(indexUbi)
        }

        // Configurar Spinner Estado
        val estados = listOf("Todos los estados") + EstadoTorneo.values().map { it.name.lowercase().replaceFirstChar(Char::titlecase) }
        val adapterEst = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, estados)
        adapterEst.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstado.adapter = adapterEst
        
        val indexEst = estados.indexOf(filtroEstado)
        if (indexEst >= 0) spinnerEstado.setSelection(indexEst)

        // Setear valores actuales
        etNombre.setText(filtroNombre)
        etDescripcion.setText(filtroDescripcion)
        etFechaInicio.setText(filtroFecha)

        etFechaInicio.setOnClickListener {
            val calendario = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                etFechaInicio.setText("%04d-%02d-%02d".format(y, m + 1, d))
            }, calendario[Calendar.YEAR], calendario[Calendar.MONTH], calendario[Calendar.DAY_OF_MONTH]).show()
        }

        btnLimpiar.setOnClickListener {
            etNombre.setText("")
            spinnerUbicacion.setSelection(0)
            spinnerEstado.setSelection(0)
            etDescripcion.setText("")
            etFechaInicio.setText("")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Filtrar torneos")
            .setView(view)
            .setPositiveButton("Filtrar") { _, _ ->
                filtroNombre = etNombre.text.toString().trim()
                filtroUbicacion = spinnerUbicacion.selectedItem.toString()
                filtroEstado = spinnerEstado.selectedItem.toString()
                filtroDescripcion = etDescripcion.text.toString().trim()
                filtroFecha = etFechaInicio.text.toString()
                
                aplicarFiltros()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoAgregarTorneo() {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_agregar_torneo, null)

        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val etFechaInicio = view.findViewById<EditText>(R.id.etFechaInicio)
        val etFechaFin = view.findViewById<EditText>(R.id.etFechaFin)
        val etHoraInicio = view.findViewById<EditText>(R.id.etHoraInicio)
        val spinnerUbicacion = view.findViewById<Spinner>(R.id.spinnerUbicacion)

        val calendario = Calendar.getInstance()

        etFechaInicio.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                etFechaInicio.setText("%04d-%02d-%02d".format(y, m + 1, d))
            }, calendario[Calendar.YEAR], calendario[Calendar.MONTH], calendario[Calendar.DAY_OF_MONTH]).show()
        }

        etFechaFin.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                etFechaFin.setText("%04d-%02d-%02d".format(y, m + 1, d))
            }, calendario[Calendar.YEAR], calendario[Calendar.MONTH], calendario[Calendar.DAY_OF_MONTH]).show()
        }

        etHoraInicio.setOnClickListener {
            val h = calendario[Calendar.HOUR_OF_DAY]
            val min = calendario[Calendar.MINUTE]
            android.app.TimePickerDialog(requireContext(), { _, hSeleccionada, mSeleccionada ->
                etHoraInicio.setText("%02d:%02d".format(hSeleccionada, mSeleccionada))
            }, h, min, true).show()
        }

        MarcadorRepository().escucharMarcadores { lista ->
            val nombres = lista.filter { it.categoria.name == "TORNEO" }.map { it.nombre }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerUbicacion.adapter = adapter
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo torneo")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val torneo = Torneo(
                    idTorneo = "",
                    nombre = etNombre.text.toString(),
                    descripcion = etDescripcion.text.toString(),
                    fechaInicio = etFechaInicio.text.toString(),
                    fechaFin = etFechaFin.text.toString(),
                    horaInicio = etHoraInicio.text.toString(),
                    ubicacion = spinnerUbicacion.selectedItem?.toString() ?: ""
                )
                TorneoRepository().agregarTorneo(torneo) { exito, _ ->
                    Toast.makeText(
                        requireContext(),
                        if (exito) "Guardado" else "Error al guardar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
