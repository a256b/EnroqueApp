package com.example.apptorneosajedrez.ui.inscripciones

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptorneosajedrez.R

class MisInscripcionesAdapter(
    private val items: List<InscripcionItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_HEADER = 0
    private val VIEW_ITEM = 1

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerText: TextView = itemView.findViewById(R.id.textHeader)
        fun bind(titulo: String) {
            headerText.text = titulo
        }
    }

    class MisInscripcionesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreJugadorTextView: TextView = itemView.findViewById(R.id.textNombreInscripto)
        private val nombreTorneoTextView: TextView = itemView.findViewById(R.id.textNombreTorneo)

        fun bind(info: InscripcionInfo) {
            nombreJugadorTextView.text = info.nombreJugador
            nombreTorneoTextView.text = info.nombreTorneo
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is InscripcionItem.Header -> VIEW_HEADER
            is InscripcionItem.Data -> VIEW_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_header_torneo, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_mis_inscripciones, parent, false)
                MisInscripcionesViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is InscripcionItem.Header -> (holder as HeaderViewHolder).bind(item.titulo)
            is InscripcionItem.Data -> (holder as MisInscripcionesViewHolder).bind(item.info)
        }
    }

    override fun getItemCount(): Int = items.size
}
