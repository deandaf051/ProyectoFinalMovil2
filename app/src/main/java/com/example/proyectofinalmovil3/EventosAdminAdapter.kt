package com.example.proyectofinalmovil3.Evento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinalmovil3.R

class EventosAdminAdapter(
    private val eventos: MutableList<Evento>,
    private val claves: MutableList<String>,
    private val onEliminarClick: (String, Int) -> Unit
) : RecyclerView.Adapter<EventosAdminAdapter.EventoViewHolder>() {

    inner class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitulo: TextView = itemView.findViewById(R.id.txtTituloEventoItem)
        val txtDetalle: TextView = itemView.findViewById(R.id.txtDetalleEventoItem)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento_admin, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        val clave = claves[position]

        holder.txtTitulo.text = evento.titulo
        holder.txtDetalle.text = "${evento.mes} ${evento.dia} · ${evento.hora} · ${evento.categoria}"

        holder.btnEliminar.setOnClickListener {
            onEliminarClick(clave, position)
        }
    }

    override fun getItemCount(): Int = eventos.size

    fun eliminarLocalmente(posicion: Int) {
        eventos.removeAt(posicion)
        claves.removeAt(posicion)
        notifyItemRemoved(posicion)
    }
}
