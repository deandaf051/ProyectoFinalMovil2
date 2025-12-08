package com.example.proyectofinalmovil3

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class PerfilFragment : Fragment() {

    //declaracion de variables de entorno grafico

    lateinit var nombreUsuario: TextView
    lateinit var nombreUsuarioEditText: TextView
    lateinit var correoUsuario: TextView

    lateinit var editarPerfilButton: Button
    lateinit var actualizarButton: Button

    lateinit var eventosTextView: TextView
    lateinit var pasosTextView: TextView
    lateinit var logrosTextView: TextView

    lateinit var cerrarSesionButton: Button

    private lateinit var database: DatabaseReference

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        database = Firebase.database.reference

        //inicializacion de variables de entorno grafico
        nombreUsuario = view.findViewById(R.id.nombreUsuario)
        nombreUsuarioEditText = view.findViewById(R.id.nombreUsuarioEditText)
        correoUsuario = view.findViewById(R.id.correoUsuario)
        editarPerfilButton = view.findViewById(R.id.editarPerfilButton)
        actualizarButton = view.findViewById(R.id.actualizarButton)

        eventosTextView = view.findViewById(R.id.eventosTextView)
        pasosTextView = view.findViewById(R.id.pasosTextView)
        logrosTextView = view.findViewById(R.id.logrosTextView)

        cerrarSesionButton = view.findViewById(R.id.cerrarSesionButton)

        actualizarDatos()

        //acciones de botones
        editarPerfilButton.setOnClickListener {editarPerfilPaso1()}
        actualizarButton.setOnClickListener {editarPerfilPaso2()}
        cerrarSesionButton.setOnClickListener {cerrarSesion()}

        return view
    }

    fun cerrarSesion(){
        sharedPreferences.edit().clear().apply()
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }


    fun editarPerfilPaso1(){
        nombreUsuarioEditText.visibility = View.VISIBLE
        nombreUsuario.visibility = View.GONE
        editarPerfilButton.visibility = View.GONE
        actualizarButton.visibility = View.VISIBLE
    }

    fun editarPerfilPaso2(){
        nombreUsuarioEditText.visibility = View.GONE
        nombreUsuario.visibility = View.VISIBLE
        editarPerfilButton.visibility = View.VISIBLE
        actualizarButton.visibility = View.GONE

        val name = nombreUsuarioEditText.text.toString()
        val email = correoUsuario.text.toString()

        database.child("usuarios").child(email).child("nombre").setValue(name)
        sharedPreferences.edit().putString("name", name).apply()


        actualizarDatos()
    }

    fun actualizarDatos() {
        val name = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "")

        nombreUsuario.text = name
        correoUsuario.text = email
        nombreUsuarioEditText.text = name
    }
}
