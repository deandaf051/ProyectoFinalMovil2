package com.example.proyectofinalmovil3.Evento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinalmovil3.R
import com.google.android.material.chip.Chip

class EventosAdapter(
    private val listaEventos: List<Evento>,
    private val onItemClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    inner class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMes: TextView = itemView.findViewById(R.id.txtMes)
        val txtDia: TextView = itemView.findViewById(R.id.txtDia)
        val txtTituloEvento: TextView = itemView.findViewById(R.id.txtTituloEvento)
        val txtDescripcionEvento: TextView = itemView.findViewById(R.id.txtDescripcionEvento)
        val chipCategoria: Chip = itemView.findViewById(R.id.chipCategoria)
        val txtInscritos: TextView = itemView.findViewById(R.id.txtInscritos)
        val txtHora: TextView = itemView.findViewById(R.id.txtHora)
        val txtLugar: TextView = itemView.findViewById(R.id.txtLugar)
        val fecha_container: LinearLayout = itemView.findViewById(R.id.fecha_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = listaEventos[position]

        holder.txtMes.text = evento.mes
        holder.txtDia.text = evento.dia
        holder.txtTituloEvento.text = evento.titulo
        holder.txtDescripcionEvento.text = evento.descripcion
        holder.chipCategoria.text = evento.categoria
        holder.txtInscritos.text = "${evento.inscritos} inscritos"
        holder.txtHora.text = evento.hora
        holder.txtLugar.text = evento.lugar

        cambioColor(holder, position)

        holder.itemView.setOnClickListener {
            onItemClick(evento)
        }
    }

    private fun cambioColor(holder: EventoViewHolder, position: Int){
        val evento = listaEventos[position]

        when(evento.categoria){
            "Limpieza" -> {
                return
            }
            "Reciclaje" -> {
                holder.fecha_container.setBackgroundResource(R.drawable.date_background_yellow)
                holder.chipCategoria.setChipBackgroundColorResource(R.color.colorYellow)
                holder.txtDia.setTextColor(holder.itemView.context.getColor(R.color.colorYellow))
                holder.txtMes.setTextColor(holder.itemView.context.getColor(R.color.colorYellow))
            }
            "Reforestacion" -> {
                holder.fecha_container.setBackgroundResource(R.drawable.date_background_green)
                holder.chipCategoria.setChipBackgroundColorResource(R.color.colorAccentGreen)
                holder.txtDia.setTextColor(holder.itemView.context.getColor(R.color.colorAccentGreen))
                holder.txtMes.setTextColor(holder.itemView.context.getColor(R.color.colorAccentGreen))
            }
        }
    }

    override fun getItemCount(): Int = listaEventos.size
}
