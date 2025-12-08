package com.example.proyectofinalmovil3

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinalmovil3.Evento.Evento
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ActivityAdmin : AppCompatActivity() {

    // Firebase: referencia a "eventos"
    private lateinit var database: DatabaseReference

    // UI
    private lateinit var inputMes: TextInputLayout
    private lateinit var inputDia: TextInputLayout
    private lateinit var inputTitulo: TextInputLayout
    private lateinit var inputDescripcion: TextInputLayout
    private lateinit var spinnerCategoria: Spinner
    private lateinit var inputInscritos: TextInputLayout
    private lateinit var inputHora: TextInputLayout
    private lateinit var inputLugar: TextInputLayout

    private lateinit var btnGuardar: Button
    private lateinit var btnBuscar: Button
    private lateinit var btnEditar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnLimpiar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        supportActionBar?.title = "Admin eventos"

        // 1) Firebase -> nodo "eventos"
        database = FirebaseDatabase.getInstance().reference.child("eventos")

        // 2) Referencias UI
        inputMes = findViewById(R.id.inputMes)
        inputDia = findViewById(R.id.inputDia)
        inputTitulo = findViewById(R.id.inputTitulo)
        inputDescripcion = findViewById(R.id.inputDescripcion)
        spinnerCategoria = findViewById(R.id.spinnerCategoria)
        inputInscritos = findViewById(R.id.inputInscritos)
        inputHora = findViewById(R.id.inputHora)
        inputLugar = findViewById(R.id.inputLugar)

        btnGuardar = findViewById(R.id.btnGuardar)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnEditar = findViewById(R.id.btnEditar)
        btnEliminar = findViewById(R.id.btnEliminar)
        btnLimpiar = findViewById(R.id.btnLimpiar)

        // 3) Spinner de categorías (texto EXACTO)
        val categorias = arrayOf("Limpieza", "Reciclaje", "Reforestacion")
        val adapterSpinner = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categorias
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterSpinner

        // 4) Listeners de botones
        btnGuardar.setOnClickListener { guardarEvento() }
        btnBuscar.setOnClickListener { buscarEvento() }
        btnEditar.setOnClickListener { editarEvento() }
        btnEliminar.setOnClickListener { eliminarEvento() }
        btnLimpiar.setOnClickListener { limpiarCampos() }
    }

    private fun getText(input: TextInputLayout): String =
        input.editText?.text?.toString()?.trim() ?: ""

    // ---------------- VALIDACIONES ----------------

    private fun validarCampos(): Boolean {
        val mes = getText(inputMes)
        val dia = getText(inputDia)
        val titulo = getText(inputTitulo)
        val descripcion = getText(inputDescripcion)
        val inscritosStr = getText(inputInscritos)
        val hora = getText(inputHora)
        val lugar = getText(inputLugar)

        if (mes.isEmpty() || dia.isEmpty() || titulo.isEmpty() ||
            descripcion.isEmpty() || inscritosStr.isEmpty() ||
            hora.isEmpty() || lugar.isEmpty()
        ) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (mes.length != 3) {
            Toast.makeText(this, "El mes debe tener exactamente 3 letras (Ej: ENE)", Toast.LENGTH_SHORT).show()
            return false
        }

        if (inscritosStr.toIntOrNull() == null) {
            Toast.makeText(this, "Inscritos debe ser un número entero", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun construirEvento(): Evento {
        val mes = getText(inputMes)
        val dia = getText(inputDia)
        val titulo = getText(inputTitulo)
        val descripcion = getText(inputDescripcion)
        val categoria = spinnerCategoria.selectedItem.toString()
        val inscritos = getText(inputInscritos).toInt()
        val hora = getText(inputHora)
        val lugar = getText(inputLugar)

        return Evento(
            mes = mes,
            dia = dia,
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoria,
            inscritos = inscritos,
            hora = hora,
            lugar = lugar
        )
    }

    // ---------------- CRUD ----------------

    /** GUARDAR sin sobrescribir si el título ya existe */
    private fun guardarEvento() {
        if (!validarCampos()) return

        val evento = construirEvento()
        val clave = evento.titulo

        // Primero verificar si ya existe un evento con ese título
        database.child(clave).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Toast.makeText(
                        this,
                        "Ese título ya está en uso, prueba con otro.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                // No existe -> guardamos
                database.child(clave).setValue(evento)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Evento guardado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar título: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun buscarEvento() {
        val titulo = getText(inputTitulo)
        if (titulo.isEmpty()) {
            Toast.makeText(this, "Escribe el título para buscar", Toast.LENGTH_SHORT).show()
            return
        }

        database.child(titulo).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    Toast.makeText(this, "No existe evento con ese título", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val evento = snapshot.getValue(Evento::class.java)
                if (evento == null) {
                    Toast.makeText(this, "Error al leer el evento", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Rellenamos los campos
                inputMes.editText?.setText(evento.mes)
                inputDia.editText?.setText(evento.dia)
                inputDescripcion.editText?.setText(evento.descripcion)
                inputInscritos.editText?.setText(evento.inscritos.toString())
                inputHora.editText?.setText(evento.hora)
                inputLugar.editText?.setText(evento.lugar)

                val index = (spinnerCategoria.adapter as ArrayAdapter<String>)
                    .getPosition(evento.categoria)
                if (index >= 0) spinnerCategoria.setSelection(index)

                Toast.makeText(this, "Evento encontrado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al buscar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editarEvento() {
        if (!validarCampos()) return

        val evento = construirEvento()
        val clave = evento.titulo

        database.child(clave).setValue(evento)
            .addOnSuccessListener {
                Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarEvento() {
        val titulo = getText(inputTitulo)
        if (titulo.isEmpty()) {
            Toast.makeText(this, "Escribe el título para eliminar", Toast.LENGTH_SHORT).show()
            return
        }

        database.child(titulo).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Evento eliminado", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarCampos() {
        inputMes.editText?.setText("")
        inputDia.editText?.setText("")
        inputTitulo.editText?.setText("")
        inputDescripcion.editText?.setText("")
        inputInscritos.editText?.setText("")
        inputHora.editText?.setText("")
        inputLugar.editText?.setText("")
        spinnerCategoria.setSelection(0)
    }
}
