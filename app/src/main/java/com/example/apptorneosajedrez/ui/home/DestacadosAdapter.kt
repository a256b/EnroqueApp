package com.example.apptorneosajedrez.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.model.Torneo

sealed class DestacadoItem {
    data class Header(val titulo: String) : DestacadoItem()
    data class TorneoData(val torneo: Torneo) : DestacadoItem()
}

class DestacadosAdapter(
    private val items: List<DestacadoItem>,
    private val onEliminarFavorito: (Torneo) -> Unit,
    private val onTorneoClick: (Torneo) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_HEADER = 0
    private val VIEW_TORNEO = 1

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerText: TextView = itemView.findViewById(R.id.textHeader)
        fun bind(text: String) {
            headerText.text = text
        }
    }

    inner class DestacadoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.textNombreTorneo)
        private val ubicacionTextView: TextView = itemView.findViewById(R.id.textUbicacionTorneo)
        private val inscriptosTextView: TextView = itemView.findViewById(R.id.textInscriptosTorneo)
        private val estrellaImageView: ImageView = itemView.findViewById(R.id.imgEstrella)

        fun bind(torneo: Torneo) {
            nombreTextView.text = torneo.nombre
            ubicacionTextView.text = torneo.ubicacion
            
            val numInscriptos = torneo.jugadores.size
            inscriptosTextView.text = "Inscriptos: $numInscriptos"
            
            val context = itemView.context
            if (numInscriptos == 8) {
                inscriptosTextView.setTextColor(ContextCompat.getColor(context, R.color.rojo))
            } else {
                inscriptosTextView.setTextColor(ContextCompat.getColor(context, R.color.estado_torneo_activo))
            }

            estrellaImageView.setImageResource(R.drawable.ic_star2)

            estrellaImageView.setOnClickListener {
                onEliminarFavorito(torneo)
            }

            itemView.setOnClickListener {
                onTorneoClick(torneo)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DestacadoItem.Header -> VIEW_HEADER
            is DestacadoItem.TorneoData -> VIEW_TORNEO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_torneo, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_torneo, parent, false)
                DestacadoViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DestacadoItem.Header -> (holder as HeaderViewHolder).bind(item.titulo)
            is DestacadoItem.TorneoData -> (holder as DestacadoViewHolder).bind(item.torneo)
        }
    }

    override fun getItemCount(): Int = items.size
}
