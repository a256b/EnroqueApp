package com.example.apptorneosajedrez.ui.torneos

import android.content.Context
import android.content.SharedPreferences
import android.widget.ImageView
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.model.Torneo

const val PREF_NAME = "torneos_prefs"
const val KEY_TORNEO_DESTACADO = "torneos_destacados"

sealed class TorneoItem{
    data class Header(val titulo: String) : TorneoItem()
    data class TorneoData(val torneo: Torneo) : TorneoItem()
}

class TorneoAdapter(
    private val items: List<TorneoItem>,
    private val context: Context,
    private val onTorneoClick: (Torneo) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_HEADER = 0
    private val VIEW_TORNEO = 1

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerText: TextView = itemView.findViewById(R.id.textHeader)
        fun bind(text: String) {
            headerText.text = text
        }
    }

    inner class TorneoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.textNombreTorneo)
        private val estrellaImageView: ImageView = itemView.findViewById(R.id.imgEstrella)
        private val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        fun bind(torneo: Torneo) {
            nombreTextView.text = torneo.nombre
            itemView.setOnClickListener { onTorneoClick(torneo) }

            val favoritos = sharedPreferences.getStringSet(KEY_TORNEO_DESTACADO, setOf())?.toMutableSet()
                ?: mutableSetOf()
            val esFavorito = favoritos.contains(torneo.nombre)

            estrellaImageView.setImageResource(
                if (esFavorito) R.drawable.ic_star2 else R.drawable.ic_star
            )

            estrellaImageView.setOnClickListener {
                val nuevos = sharedPreferences.getStringSet(KEY_TORNEO_DESTACADO, setOf())?.toMutableSet()
                    ?: mutableSetOf()

                if (nuevos.contains(torneo.nombre)) {
                    nuevos.remove(torneo.nombre)
                    Toast.makeText(context, "Torneo desmarcado", Toast.LENGTH_SHORT).show()
                } else {
                    nuevos.add(torneo.nombre)
                    Toast.makeText(context, "★ Torneo destacado", Toast.LENGTH_SHORT).show()
                }

                sharedPreferences.edit().putStringSet(KEY_TORNEO_DESTACADO, nuevos).apply()
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]){
            is TorneoItem.Header -> VIEW_HEADER
            is TorneoItem.TorneoData -> VIEW_TORNEO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            VIEW_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_torneo, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_torneo, parent, false)
                TorneoViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]){
            is TorneoItem.Header -> (holder as HeaderViewHolder).bind(item.titulo)
            is TorneoItem.TorneoData -> (holder as TorneoViewHolder).bind(item.torneo)
        }
    }

    override fun getItemCount(): Int = items.size
}
