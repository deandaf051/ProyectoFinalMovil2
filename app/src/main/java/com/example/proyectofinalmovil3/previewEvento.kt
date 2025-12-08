package com.example.proyectofinalmovil3

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectofinalmovil3.Evento.Evento
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.database

class previewEvento : AppCompatActivity() {

    // Declaración de variables de entorno gráfico
    lateinit var tituloTextView: TextView
    lateinit var descripcionTextView: TextView
    lateinit var mesTextView: TextView
    lateinit var diaTextView: TextView
    lateinit var horaTextView: TextView
    lateinit var categoriaTextView: TextView
    lateinit var lugarTextView: TextView
    lateinit var inscritosTextView: TextView

    lateinit var inscribirseButton: Button

    lateinit var evento: Evento

    private lateinit var database: DatabaseReference

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preview_evento)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val intent = getIntent()

        database = Firebase.database.reference
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)


        evento = intent.getSerializableExtra("evento") as Evento

        // Inicialización de variables de entorno gráfico
        tituloTextView = findViewById(R.id.tituloTextView)
        descripcionTextView = findViewById(R.id.descripcionTextView)
        mesTextView = findViewById(R.id.mesTextView)
        diaTextView = findViewById(R.id.diaTextView)
        horaTextView = findViewById(R.id.horaTextView)
        categoriaTextView = findViewById(R.id.categoriaTextView)
        lugarTextView = findViewById(R.id.lugarTextView)
        inscritosTextView = findViewById(R.id.inscritosTextView)

        inscribirseButton = findViewById(R.id.inscribirseButton)
        comprobarInscripcion()

        actualizarDatos()

        inscribirseButton.setOnClickListener { inscribirse() }

    }

    fun inscribirse(){
        database.child("eventos").child(evento.titulo).child("inscritos").setValue(evento.inscritos + 1)
        //val lista = listOf("evento1","evento2","evento3","evento4","evento5","evento6","evento7","evento8","evento9","evento10")
        //database.child("usuarios").child(sharedPreferences.getString("email", "")!!).child("eventos").setValue(lista)

        //obtener lista
        database.child("usuarios").child(sharedPreferences.getString("email", "")!!).child("eventos").get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val tipo = object : GenericTypeIndicator<List<String>>() {}
                var lista = dataSnapshot.getValue(tipo)

                lista = lista?.plus(evento.titulo)
                database.child("usuarios").child(sharedPreferences.getString("email", "")!!).child("eventos").setValue(lista)
            }else{
                val lista = listOf(evento.titulo)
                database.child("usuarios").child(sharedPreferences.getString("email", "")!!).child("eventos").setValue(lista)
            }
        }

        finish()
    }

    fun comprobarInscripcion(){
        database.child("usuarios").child(sharedPreferences.getString("email", "")!!).child("eventos").get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val tipo = object : GenericTypeIndicator<List<String>>() {}
                var lista = dataSnapshot.getValue(tipo)
                if (lista?.contains(evento.titulo) == true) {
                    inscribirseButton.text = "Inscrito"
                    inscribirseButton.isEnabled = false
                }
            }
        }
    }

    fun actualizarDatos(){
        tituloTextView.text = evento.titulo
        descripcionTextView.text = evento.descripcion
        mesTextView.text = evento.mes
        diaTextView.text = evento.dia
        horaTextView.text = evento.hora
        categoriaTextView.text = evento.categoria
        lugarTextView.text = evento.lugar
        inscritosTextView.text = evento.inscritos.toString()
    }
}