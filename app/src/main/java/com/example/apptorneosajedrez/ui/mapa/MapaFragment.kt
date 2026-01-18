package com.example.apptorneosajedrez.ui.mapa

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.apptorneosajedrez.databinding.FragmentMapaBinding
import com.example.apptorneosajedrez.model.Marcador
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.model.Categoria
import com.google.android.gms.maps.model.BitmapDescriptorFactory


class MapaFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapaBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private val viewModel: MapaViewModel by viewModels()

    private var filtroNombre: String = ""
    private var filtroCategoria: String = "Ambas categorías"
    private var filtroDescripcion: String = ""
    private var filtroDescuento: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializarMapa()
        observarMarcadores()
        binding.btnFiltro.setOnClickListener {
            mostrarDialogoFiltro()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoContents(marker: com.google.android.gms.maps.model.Marker): View? {
                val marcador = viewModel.marcadores.value?.find {
                    it.nombre == marker.title &&
                            it.latitud == marker.position.latitude &&
                            it.longitud == marker.position.longitude
                }

                return if (marcador?.categoria == Categoria.COMERCIO) {
                    val inflater = LayoutInflater.from(requireContext())
                    val view = inflater.inflate(R.layout.info_comercio, null)

                    val tvTitulo = view.findViewById<TextView>(R.id.tvTitulo)
                    val tvDescuento = view.findViewById<TextView>(R.id.tvDescuento)
                    val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcion)

                    tvTitulo.text = marker.title

                    val snippet = marker.snippet ?: ""
                    val partes = snippet.split("\n")

                    val desc = partes.getOrNull(0) ?: ""
                    val descripcion = partes.getOrNull(1) ?: ""

                    tvDescuento.text = desc
                    tvDescripcion.text = descripcion

                    view
                } else {
                    // TODO: por si se muestra otra cosa en los otros markers
                    null
                }
            }

            override fun getInfoWindow(marker: com.google.android.gms.maps.model.Marker): View? {
                return null
            }
        })

        googleMap.setOnMapLongClickListener { latLng ->
            mostrarDialogoAgregarMarcador(latLng)
        }

        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }
    }



    override fun onResume() {
        super.onResume()

        if (::googleMap.isInitialized) {
            val marcadoresActuales = viewModel.marcadores.value
            if (marcadoresActuales != null) {
                mostrarMarcadoresEnMapa(marcadoresActuales)
            }
        }
    }

    private fun inicializarMapa() {
        val mapFragment =
            childFragmentManager.findFragmentById(binding.navMapa.id) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun observarMarcadores() {
        viewModel.marcadores.observe(viewLifecycleOwner) { marcadores ->
            if (::googleMap.isInitialized) {
                mostrarMarcadoresEnMapa(marcadores)
            }
        }
    }

    private fun mostrarMarcadoresEnMapa(marcadores: List<Marcador>) {
        googleMap.clear()

        Toast.makeText(
            requireContext(),
            if(marcadores.size==0){
                "No existen lugares con esas características"
            }else{
                if(marcadores.size==1){
                    "${marcadores.size} lugar encontrado"
                }else{
                    "${marcadores.size} lugares encontrados"
                }
            },
            Toast.LENGTH_SHORT
        ).show()

        for (marcador in marcadores) {
            val posicion = LatLng(marcador.latitud, marcador.longitud)

            val color = when (marcador.categoria) {
                Categoria.TORNEO -> BitmapDescriptorFactory.HUE_RED
                Categoria.COMERCIO -> BitmapDescriptorFactory.HUE_BLUE
            }

            val options = MarkerOptions()
                .position(posicion)
                .title(marcador.nombre)
                .icon(BitmapDescriptorFactory.defaultMarker(color))

            if (marcador.categoria == Categoria.COMERCIO) {

                val partes = mutableListOf<String>()

                marcador.descuento?.let {
                    partes.add("Descuento: $it%")
                }

                marcador.descripcion?.let {
                    if (it.isNotBlank()) partes.add("Descripción: $it")
                }

                options.snippet(
                    partes.joinToString("\n")
                )
            }


            googleMap.addMarker(options)
        }

        centrarCamaraEnPrimerMarcador(marcadores)
    }

    private fun centrarCamaraEnPrimerMarcador(marcadores: List<Marcador>) {
        if (marcadores.isEmpty()) return
        val primerMarcador = LatLng(marcadores[0].latitud, marcadores[0].longitud)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(primerMarcador, 12f))
    }

    private fun mostrarDialogoAgregarMarcador(latLng: LatLng) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_agregar_marcador, null)

        val inputNombre = dialogView.findViewById<EditText>(R.id.editTextNombre)
        val spinnerCategoria = dialogView.findViewById<Spinner>(R.id.spinnerCategoria)

        val inputDescripcion = dialogView.findViewById<EditText>(R.id.editTextDescripcion)
        val inputDescuento = dialogView.findViewById<EditText>(R.id.editTextDescuento)

        val categorias = Categoria.values()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categorias.map { it.name.lowercase().replaceFirstChar(Char::titlecase) }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter

        inputDescripcion.isEnabled = false
        inputDescuento.isEnabled = false
        spinnerCategoria.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val seleccionado = categorias[position]
                val esComercio = seleccionado == Categoria.COMERCIO
                inputDescripcion.isEnabled = esComercio
                inputDescuento.isEnabled = esComercio
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo marcador")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString().trim()
                val categoriaSeleccionada = categorias[spinnerCategoria.selectedItemPosition]
                val descripcion = if (categoriaSeleccionada == Categoria.COMERCIO) inputDescripcion.text.toString().trim() else null
                val descuento = if (categoriaSeleccionada == Categoria.COMERCIO) inputDescuento.text.toString().toIntOrNull() else null


                if (nombre.isNotEmpty()) {

                    guardarMarcador(nombre, descripcion, descuento, latLng, categoriaSeleccionada)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "El nombre no puede estar vacío",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarMarcador(nombre: String, descripcion: String?, descuento: Int?, latLng: LatLng, categoriaSeleccionada: Categoria) {
        val marcador = Marcador(
            nombre = nombre, descripcion = descripcion, descuento = descuento ,latitud = latLng.latitude, longitud = latLng.longitude, categoria = categoriaSeleccionada
        )

        viewModel.agregarMarcador(marcador) { exito ->
            val mensaje = if (exito) "Marcador guardado" else "Error al guardar"
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoFiltro() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filtro_mapa, null)

        val spinnerCategoria = dialogView.findViewById<Spinner>(R.id.spinnerCategoriaFiltro)
        val inputNombre = dialogView.findViewById<EditText>(R.id.editTextNombreFiltro)
        val inputDescripcion = dialogView.findViewById<EditText>(R.id.editTextDescripcionFiltro)
        val inputDescuento = dialogView.findViewById<EditText>(R.id.editTextDescuentoFiltro)
        val btnLimpiar = dialogView.findViewById<Button>(R.id.btnLimpiarFiltros)

        val categorias = listOf("Ambas categorías") + Categoria.values().map {
            it.name.lowercase().replaceFirstChar(Char::titlecase)
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter

        inputNombre.setText(filtroNombre)
        inputDescripcion.setText(filtroDescripcion)
        inputDescuento.setText(filtroDescuento?.toString() ?: "")

        val indexCategoria = categorias.indexOf(filtroCategoria)
        if (indexCategoria >= 0) spinnerCategoria.setSelection(indexCategoria)

        spinnerCategoria.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val seleccion = categorias[position]
                val esTorneo = seleccion.equals("Torneo", ignoreCase = true)
                inputDescuento.isEnabled = !esTorneo
                if (esTorneo) {
                    inputDescuento.setText("")
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        AlertDialog.Builder(requireContext())
            .setTitle("Filtrar marcadores")
            .setView(dialogView)
            .setPositiveButton("Filtrar") { _, _ ->
                filtroNombre = inputNombre.text.toString().trim()
                filtroCategoria = spinnerCategoria.selectedItem.toString()
                filtroDescripcion = inputDescripcion.text.toString().trim()
                filtroDescuento = inputDescuento.text.toString().toIntOrNull()

                aplicarFiltro(filtroNombre, filtroCategoria, filtroDescripcion, filtroDescuento)
            }
            .setNegativeButton("Cancelar", null)
            .show()

        btnLimpiar.setOnClickListener {
            spinnerCategoria.setSelection(0)
            inputNombre.setText("")
            inputDescripcion.setText("")
            inputDescuento.setText("")
        }
    }

    private fun aplicarFiltro(nombre: String, categoria: String, descFiltro: String, descuentoFiltro: Int?) {
        val todos = viewModel.marcadores.value ?: return

        val filtrados = todos.filter { marcador ->
            val coincideNombre = nombre.isEmpty() || marcador.nombre.contains(nombre, ignoreCase = true)
            val coincideCategoria = categoria == "Ambas categorías" || marcador.categoria.name.equals(categoria.uppercase(), ignoreCase = true)
            val coincideDescripcion =
                descFiltro.isEmpty() ||
                        (marcador.descripcion?.contains(descFiltro, ignoreCase = true) == true)

            val coincideDescuento =
                descuentoFiltro == null ||
                        marcador.descuento == descuentoFiltro

            coincideNombre &&
                    coincideCategoria &&
                    coincideDescripcion &&
                    coincideDescuento
        }

        mostrarMarcadoresEnMapa(filtrados)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}