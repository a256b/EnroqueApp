package com.example.apptorneosajedrez.ui.torneos

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.data.MarcadorRepository
import com.example.apptorneosajedrez.data.TorneoRepository
import com.example.apptorneosajedrez.databinding.FragmentTorneosBinding
import com.example.apptorneosajedrez.model.EstadoTorneo
import com.example.apptorneosajedrez.model.Torneo
import java.util.Calendar

class TorneosFragment : Fragment() {

    private var _binding: FragmentTorneosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TorneoViewModel by viewModels()
    private var torneosList: List<Torneo> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTorneosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observarTorneos()
        binding.fabAgregarTorneo.setOnClickListener {
            mostrarDialogoAgregarTorneo()
        }
    }

    private fun observarTorneos() {
        viewModel.torneos.observe(viewLifecycleOwner) { lista ->
            torneosList = lista
            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        val activos = torneosList.filter { it.estado == EstadoTorneo.ACTIVO }
        val proximos = torneosList.filter { it.estado == EstadoTorneo.PROXIMO }
        val finalizados = torneosList.filter { it.estado == EstadoTorneo.FINALIZADO }
        val suspendidos = torneosList.filter { it.estado == EstadoTorneo.SUSPENDIDO }

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
            TimePickerDialog(requireContext(), { _, h, m ->
                etHoraInicio.setText("%02d:%02d".format(h, m))
            }, calendario[Calendar.HOUR_OF_DAY], calendario[Calendar.MINUTE], true).show()
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
                    idTorneo = (0..99999).random(),
                    nombre = etNombre.text.toString(),
                    descripcion = etDescripcion.text.toString(),
                    fechaInicio = etFechaInicio.text.toString(),
                    fechaFin = etFechaFin.text.toString(),
                    horaInicio = etHoraInicio.text.toString(),
                    ubicacion = spinnerUbicacion.selectedItem?.toString() ?: ""
                )
                TorneoRepository().agregarTorneo(torneo) { exito ->
                    Toast.makeText(requireContext(), if (exito) "Guardado" else "Error al guardar", Toast.LENGTH_SHORT).show()
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
