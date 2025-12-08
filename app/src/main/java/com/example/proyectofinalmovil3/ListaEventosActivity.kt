package com.example.proyectofinalmovil3

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinalmovil3.Evento.Evento
import com.example.proyectofinalmovil3.Evento.EventosAdminAdapter
import com.google.firebase.database.*

class ListaEventosActivity : AppCompatActivity() {

    private lateinit var recyclerEventos: RecyclerView
    private lateinit var adapter: EventosAdminAdapter

    private lateinit var database: DatabaseReference
    private val listaEventos = mutableListOf<Evento>()
    private val listaClaves = mutableListOf<String>()   // títulos / keys en Firebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_eventos)

        supportActionBar?.title = "Lista de eventos"

        recyclerEventos = findViewById(R.id.recyclerEventosAdmin)
        recyclerEventos.layoutManager = LinearLayoutManager(this)

        adapter = EventosAdminAdapter(listaEventos, listaClaves) { clave, posicion ->
            eliminarEvento(clave, posicion)
        }
        recyclerEventos.adapter = adapter

        database = FirebaseDatabase.getInstance().reference.child("eventos")

        cargarEventos()
    }

    private fun cargarEventos() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaEventos.clear()
                listaClaves.clear()

                for (child in snapshot.children) {
                    val evento = child.getValue(Evento::class.java)
                    val clave = child.key
                    if (evento != null && clave != null) {
                        listaEventos.add(evento)
                        listaClaves.add(clave)
                    }
                }

                adapter.notifyDataSetChanged()

                if (listaEventos.isEmpty()) {
                    Toast.makeText(
                        this@ListaEventosActivity,
                        "No hay eventos registrados",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ListaEventosActivity,
                    "Error al cargar eventos: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun eliminarEvento(clave: String, posicion: Int) {
        database.child(clave).removeValue()
            .addOnSuccessListener {
                adapter.eliminarLocalmente(posicion)
                Toast.makeText(this, "Evento eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
