package com.example.proyectofinalmovil3

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.example.proyectofinalmovil3.ActivityHome


class MainActivity : AppCompatActivity() {

    //variables del entorno grafico
    private lateinit var iniciarSesionButtonVista: Button
    private lateinit var registrarseButtonVista: Button

    private lateinit var iniciarSesionButton: Button
    private lateinit var registrarseButton: Button

    private lateinit var correoInicioSesionInput: TextInputLayout
    private lateinit var contraseñaInicioSesionInput: TextInputLayout

    private lateinit var nombreRegistroInput: TextInputLayout
    private lateinit var correoRegistroInput: TextInputLayout
    private lateinit var contraseñaRegistroInput: TextInputLayout

    private lateinit var vistaInicioSesion: LinearLayout
    private lateinit var vistaRegistro: LinearLayout

    //base de datos en firebase
    private lateinit var database: DatabaseReference

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        comprobarInicioSesion()

        //inicializacion de variables del entorno grafico
        iniciarSesionButtonVista = findViewById(R.id.iniciarSesionButtonVista2)
        registrarseButtonVista = findViewById(R.id.registrarseButtonVista1)
        iniciarSesionButton = findViewById(R.id.iniciarSesionButton)
        registrarseButton = findViewById(R.id.registrarseButton)
        correoInicioSesionInput = findViewById(R.id.correoInicioSesionInputLayout)
        contraseñaInicioSesionInput = findViewById(R.id.contrasenaInicioSesionInputLayout)
        nombreRegistroInput = findViewById(R.id.nombreRegistroInputLayout)
        correoRegistroInput = findViewById(R.id.correoRegistroInputLayout)
        contraseñaRegistroInput = findViewById(R.id.contrasenaRegistroInputLayout)
        vistaInicioSesion = findViewById(R.id.inicioSesionLinearLayout)
        vistaRegistro = findViewById(R.id.registroLinearLayout)

        //inicializacion de base de datos
        database = Firebase.database.reference

        //acciones de botones
        iniciarSesionButtonVista.setOnClickListener { vistaInicioSesion() }
        registrarseButtonVista.setOnClickListener { vistaRegistro() }

        iniciarSesionButton.setOnClickListener { iniciarSesion() }
        registrarseButton.setOnClickListener { registrarse() }
    }

    private fun registrarse(){
        val nombre = nombreRegistroInput.editText?.text.toString()
        var correo = correoRegistroInput.editText?.text.toString()
        val contraseña = contraseñaRegistroInput.editText?.text.toString()
        if (comprobarCamposRegistro()) {
            if(correo.endsWith(".com")){
                correo = correo.dropLast(4)
            }
            Log.d("DepuracionLogin", "Clave generada en Registro: '$correo'")
            // Realizar la autenticación con Firebase
            database.child("usuarios").child(correo).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    // El usuario ya existe
                    Toast.makeText(this, "El correo ya esta en uso", Toast.LENGTH_SHORT).show()
                }else{
                    //El usuario no existe
                    database.child("usuarios").child(correo).child("nombre").setValue(nombre)
                    database.child("usuarios").child(correo).child("contraseña").setValue(contraseña)
                    val estadisticasIniciales = mapOf(
                        "eventosAsistidos" to 0,
                        "pasosTotales" to 0,
                        "eventosLimpieza" to 0,
                        "eventosReciclaje" to 0,
                        "eventosReforestacion" to 0
                    )
                    // Crea el sub-nodo "estadisticas" con valores en 0
                    database.child("usuarios").child(correo).child("estadisticas").setValue(estadisticasIniciales)
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                    val editor = sharedPreferences.edit()
                    editor.putString("email", correo)
                    editor.putString("name",nombre)
                    editor.apply()

                    comprobarInicioSesion()
                }
            }
        }
    }

    private fun iniciarSesion(){
        var correo = correoInicioSesionInput.editText?.text.toString()
        val contraseña = contraseñaInicioSesionInput.editText?.text.toString()
        if (comprobarCamposInicioSesion()) {
            if(correo.endsWith(".com")){
                correo = correo.dropLast(4)
            }
            // Realizar la autenticación con Firebase
            database.child("usuarios").child(correo).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val contraseñaGuardada =
                        dataSnapshot.child("contraseña").getValue(String::class.java)
                    if (contraseña == contraseñaGuardada) {
                        // Autenticación exitosa
                        val name = dataSnapshot.child("nombre").getValue(String::class.java)

                        //guardar datos de sesion para mantenerlos
                        val editor = sharedPreferences.edit()
                        editor.putString("email", correo)
                        editor.putString("name",name)
                        editor.apply()

                        comprobarInicioSesion()
                    } else {
                        // Contraseña incorrecta
                        Toast.makeText(this, "Datos incorrectos", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    // El usuario no existe
                    Toast.makeText(this, "Datos incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun comprobarCamposInicioSesion(): Boolean {
        val correo = correoInicioSesionInput.editText?.text.toString()
        val contraseña = contraseñaInicioSesionInput.editText?.text.toString()
        if (!correo.isEmpty() && !contraseña.isEmpty()) {
            return true
        }
        return false
    }

    private fun comprobarCamposRegistro(): Boolean {
        val nombre = nombreRegistroInput.editText?.text.toString()
        val correo = correoRegistroInput.editText?.text.toString()
        val contraseña = contraseñaRegistroInput.editText?.text.toString()
        if (!nombre.isNullOrEmpty() && !correo.isNullOrEmpty() && !contraseña.isNullOrEmpty()) {
            return true
        }
        return false
    }


    private fun comprobarInicioSesion() {
        val savedEmail = sharedPreferences.getString("email", "")
        val savedName = sharedPreferences.getString("name", "")

        if (!savedName.isNullOrEmpty() && !savedEmail.isNullOrEmpty()) {
            Toast.makeText(this, "Bienvenido $savedName", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ActivityHome::class.java)
            startActivity(intent)
            finish() //
             // evita volver atrás al login
        }
    }


    private fun vistaRegistro(){
        vistaInicioSesion.visibility = View.GONE
        vistaRegistro.visibility = View.VISIBLE

    }

    private fun vistaInicioSesion(){
        vistaInicioSesion.visibility = View.VISIBLE
        vistaRegistro.visibility = View.GONE
    }
}