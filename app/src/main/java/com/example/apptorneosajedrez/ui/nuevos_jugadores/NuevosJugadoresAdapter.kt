package com.example.apptorneosajedrez.ui.nuevos_jugadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apptorneosajedrez.databinding.ItemNuevosJugadoresBinding
import com.example.apptorneosajedrez.model.Usuario

class NuevosJugadoresAdapter(
    private var lista: List<Usuario>,
    private val listener: OnDecisionListener
) : RecyclerView.Adapter<NuevosJugadoresAdapter.UsuarioViewHolder>() {

    interface OnDecisionListener {
        fun onAceptar(usuario: Usuario)
        fun onRechazar(usuario: Usuario)
    }

    class UsuarioViewHolder(val binding: ItemNuevosJugadoresBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemNuevosJugadoresBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UsuarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = lista[position]
        holder.binding.txtNombreCompleto.text = usuario.nombreCompleto
        holder.binding.txtEmail.text = usuario.email

        holder.binding.btnAceptar.setOnClickListener {
            listener.onAceptar(usuario)
        }

        holder.binding.btnRechazar.setOnClickListener {
            listener.onRechazar(usuario)
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Usuario>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
