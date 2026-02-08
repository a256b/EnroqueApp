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
import com.example.apptorneosajedrez.model.Torneo
import com.example.apptorneosajedrez.ui.torneos.KEY_TORNEO_DESTACADO
import com.example.apptorneosajedrez.ui.torneos.PREF_NAME

class DestacadosFragment : Fragment() {

    private var _binding: FragmentDestacadosBinding? = null
    private val binding get() = _binding!!
    private val torneoRepo = TorneoRepository()

    private lateinit var destacadosAdapter: DestacadosAdapter   // 👈 NUEVO

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDestacadosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Crear adapter vacío, con callbacks
        destacadosAdapter = DestacadosAdapter(
            items = emptyList(),
            onEliminarFavorito = { torneo ->
                // Actualizamos SharedPreferences cuando se toca la estrellita
                val prefs = requireContext()
                    .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

                val favoritos = prefs
                    .getStringSet(KEY_TORNEO_DESTACADO, emptySet())
                    ?.toMutableSet() ?: mutableSetOf()

                favoritos.remove(torneo.nombre)

                prefs.edit().putStringSet(KEY_TORNEO_DESTACADO, favoritos).apply()

                Toast.makeText(requireContext(), "Torneo desmarcado", Toast.LENGTH_SHORT).show()

                // Volver a cargar la lista
                mostrarFavoritos()
            },
            onTorneoClick = { torneo ->
                val bundle = Bundle().apply {
                    putSerializable("torneo", torneo)
                }
                findNavController().navigate(
                    R.id.action_nav_home_to_detalleTorneoFragment,
                    bundle
                )
            }
        )

        // 2) Conectar RecyclerView con layoutManager y adapter
        binding.recyclerViewDestacados.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = destacadosAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        mostrarFavoritos()
    }

    private fun mostrarFavoritos() {
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val favoritos = prefs.getStringSet(KEY_TORNEO_DESTACADO, emptySet())
            ?.toMutableSet() ?: mutableSetOf()

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

            // 3) Solo actualizamos los items del adapter
            destacadosAdapter.updateItems(items)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
