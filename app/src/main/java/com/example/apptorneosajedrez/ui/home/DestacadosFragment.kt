package com.example.apptorneosajedrez.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.data.TorneoRepository
import com.example.apptorneosajedrez.databinding.FragmentDestacadosBinding
import com.example.apptorneosajedrez.model.EstadoTorneo
import com.example.apptorneosajedrez.ui.torneos.KEY_TORNEO_DESTACADO
import com.example.apptorneosajedrez.ui.torneos.PREF_NAME

class DestacadosFragment : Fragment() {

    private var _binding: FragmentDestacadosBinding? = null
    private val binding get() = _binding!!
    private val torneoRepo = TorneoRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDestacadosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mostrarFavoritos()
    }

    private fun mostrarFavoritos() {
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val favoritos = prefs.getStringSet(KEY_TORNEO_DESTACADO, emptySet())?.toMutableSet() ?: mutableSetOf()

        torneoRepo.escucharTorneos { listaTorneos ->
            val torneosFavoritos = listaTorneos.filter { favoritos.contains(it.nombre) }

            val items = mutableListOf<DestacadoItem>()
            val grupos = torneosFavoritos.groupBy { it.estado }

            val titulos = mapOf(
                EstadoTorneo.ACTIVO to "Torneos activos",
                EstadoTorneo.PROXIMO to "Próximos torneos",
                EstadoTorneo.FINALIZADO to "Torneos finalizados",
                EstadoTorneo.SUSPENDIDO to "Torneos suspendidos"
            )

            val estadosOrdenados = listOf(
                EstadoTorneo.ACTIVO,
                EstadoTorneo.PROXIMO,
                EstadoTorneo.FINALIZADO,
                EstadoTorneo.SUSPENDIDO
            )

            estadosOrdenados.forEach { estado ->
                grupos[estado]?.let { lista ->
                    if (lista.isNotEmpty()) {
                        items.add(DestacadoItem.Header(titulos[estado] ?: estado.name))
                        items.addAll(lista.map { DestacadoItem.TorneoData(it) })
                    }
                }
            }

            val adapter = DestacadosAdapter(
                items = items,
                onEliminarFavorito = { torneo ->
                    favoritos.remove(torneo.nombre)
                    prefs.edit().putStringSet(KEY_TORNEO_DESTACADO, favoritos).apply()
                    Toast.makeText(requireContext(), "Torneo desmarcado", Toast.LENGTH_SHORT).show()
                    mostrarFavoritos()
                },
                onTorneoClick = { torneo ->
                    val bundle = Bundle().apply {
                        putSerializable("torneo", torneo)
                    }
                    findNavController().navigate(R.id.action_nav_home_to_detalleTorneoFragment, bundle)
                }
            )

            _binding?.recyclerViewDestacados?.layoutManager = LinearLayoutManager(requireContext())
            _binding?.recyclerViewDestacados?.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
