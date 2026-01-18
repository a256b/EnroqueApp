package com.example.apptorneosajedrez.ui.jugadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.model.Jugador

sealed class JugadorItem {
    data class Header(val titulo: String) : JugadorItem()
    data class JugadorData(val jugador: Jugador) : JugadorItem()
}

class JugadorAdapter(
    private val items: List<JugadorItem>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_JUGADOR = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is JugadorItem.Header -> TYPE_HEADER
            is JugadorItem.JugadorData -> TYPE_JUGADOR
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_jugador, parent, false)
            JugadorViewHolder(view)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is JugadorItem.Header -> (holder as HeaderViewHolder).bind(item.titulo)
            is JugadorItem.JugadorData -> (holder as JugadorViewHolder).bind(item.jugador)
        }
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtHeader: TextView = view.findViewById(R.id.textHeader)
        fun bind(titulo: String) {
            txtHeader.text = titulo
        }
    }

    inner class JugadorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nombreTextView: TextView = view.findViewById(R.id.textNombreJugador)
        private val emailTextView: TextView = view.findViewById(R.id.txtEmail)

        fun bind(jugador: Jugador) {
            nombreTextView.text = jugador.nombre
            emailTextView.text = jugador.email
        }
    }
}
