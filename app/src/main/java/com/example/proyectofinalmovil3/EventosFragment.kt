package com.example.proyectofinalmovil3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinalmovil3.Evento.Evento
import com.example.proyectofinalmovil3.Evento.EventosAdapter
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class EventosFragment : Fragment() {

    // Declaración de variables de entorno gráfico
    lateinit var recyclerViewEventos: RecyclerView
    lateinit var chipTodos: Chip
    lateinit var chipLimpieza: Chip
    lateinit var chipReciclaje: Chip
    lateinit var chipReforestacion: Chip


    lateinit var listaEventos: MutableList<Evento>

    private lateinit var database: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_eventos, container, false)

        database = Firebase.database.reference
        listaEventos = mutableListOf()
        obtenerEventos()

        // Inicialización de variables de entorno gráfico
        recyclerViewEventos = view.findViewById(R.id.recyclerViewEventos)
        chipTodos = view.findViewById(R.id.chipTodos)
        chipLimpieza = view.findViewById(R.id.chipLimpieza)
        chipReciclaje = view.findViewById(R.id.chipReciclaje)
        chipReforestacion = view.findViewById(R.id.chipReforestacion)

        // Configuración del RecyclerView
        recyclerViewEventos.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    fun obtenerEventos() {
        database.child("eventos").get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                for (eventoSnapshot in dataSnapshot.children) {
                    val mes = eventoSnapshot.child("mes").getValue(String::class.java)
                    val dia = eventoSnapshot.child("dia").getValue(String::class.java)
                    val titulo = eventoSnapshot.child("titulo").getValue(String::class.java)
                    val descripcion = eventoSnapshot.child("descripcion").getValue(String::class.java)
                    val categoria = eventoSnapshot.child("categoria").getValue(String::class.java)
                    val inscritos = eventoSnapshot.child("inscritos").getValue(Int::class.java)
                    val hora = eventoSnapshot.child("hora").getValue(String::class.java)
                    val lugar = eventoSnapshot.child("lugar").getValue(String::class.java)

                    if (mes != null && dia != null && titulo != null && descripcion != null && categoria != null && inscritos != null && hora != null && lugar != null) {
                        val evento =
                            Evento(mes, dia, titulo, descripcion, categoria, inscritos, hora, lugar)
                        listaEventos.add(evento)
                    }
                }
                recyclerViewEventos.adapter =
                    EventosAdapter(listaEventos) { evento -> clickEvento(evento) }

                // Configuración de los Chips
                chipTodos.setOnClickListener { chipTodos() }
                chipLimpieza.setOnClickListener { chipLimpieza() }
                chipReciclaje.setOnClickListener { chipReciclaje() }
                chipReforestacion.setOnClickListener { chipReforestacion() }
            } else {
                Toast.makeText(requireContext(), "No hay eventos", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun clickEvento(evento: Evento){
        val intent = Intent(requireContext(), previewEvento::class.java)
        intent.putExtra("evento", evento)
        startActivity(intent)
    }

    fun chipTodos() {
        recyclerViewEventos.adapter = EventosAdapter(listaEventos){evento -> clickEvento(evento)}
    }
    fun chipLimpieza() {
        val listaLimpieza = listaEventos.filter { it.categoria == "Limpieza" }
        recyclerViewEventos.adapter = EventosAdapter(listaLimpieza){evento -> clickEvento(evento)}
    }
    fun chipReciclaje(){
        val listaReciclaje = listaEventos.filter { it.categoria == "Reciclaje" }
        recyclerViewEventos.adapter = EventosAdapter(listaReciclaje){evento -> clickEvento(evento)}
    }
    fun chipReforestacion(){
        val listaReforestacion = listaEventos.filter { it.categoria == "Reforestacion" }
        recyclerViewEventos.adapter = EventosAdapter(listaReforestacion){evento -> clickEvento(evento)}
    }


}
