package com.example.proyectofinalmovil3

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import java.io.File

class PerfilFragment : Fragment() {

    // Variables de entorno gráfico
    private lateinit var imageView: ImageView
    private lateinit var nombreUsuario: TextView
    private lateinit var nombreUsuarioEditText: TextView
    private lateinit var correoUsuario: TextView

    private lateinit var editarPerfilButton: Button
    private lateinit var actualizarButton: Button
    private lateinit var cambiarFotoButton: Button


    private lateinit var cerrarSesionButton: Button
    private lateinit var adminButton: Button

    // Firebase
    private lateinit var database: DatabaseReference

    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var fotoUri: Uri

    private val launcherCamara = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageView.setImageURI(fotoUri)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                abrirCamara()
            } else {
                Toast.makeText(requireContext(), "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show()
            }
        }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Firebase
        database = Firebase.database.reference

        // Inicialización de vistas
        imageView = view.findViewById(R.id.imageView)
        nombreUsuario = view.findViewById(R.id.nombreUsuario)
        nombreUsuarioEditText = view.findViewById(R.id.nombreUsuarioEditText)
        correoUsuario = view.findViewById(R.id.correoUsuario)

        editarPerfilButton = view.findViewById(R.id.editarPerfilButton)
        actualizarButton = view.findViewById(R.id.actualizarButton)
        cambiarFotoButton = view.findViewById(R.id.cambiarFotoButton)

        cerrarSesionButton = view.findViewById(R.id.cerrarSesionButton)
        adminButton = view.findViewById(R.id.adminButton)

        // Cargar datos del usuario y mostrar/ocultar botón admin
        actualizarDatos()

        // Acciones de botones
        editarPerfilButton.setOnClickListener { editarPerfilPaso1() }
        actualizarButton.setOnClickListener { editarPerfilPaso2() }

        cerrarSesionButton.setOnClickListener { cerrarSesion() }

        adminButton.setOnClickListener {
            // Ir al login de admin (o directo a ActivityAdmin si prefieres)
            val intent = Intent(requireContext(), ActivityAdmin::class.java)
            startActivity(intent)
        }
        cambiarFotoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

                abrirCamara() // ✔ permiso dado

            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA) // ❗ pedir permiso
            }

        }

        return view
    }
    private fun abrirCamara() {
        val archivo = File(requireContext().externalCacheDir, "foto_temp.jpg")
        fotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            archivo
        )

        launcherCamara.launch(fotoUri)
    }


    private fun cerrarSesion() {
        sharedPreferences.edit().clear().apply()
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun editarPerfilPaso1() {
        nombreUsuarioEditText.visibility = View.VISIBLE
        nombreUsuario.visibility = View.GONE
        editarPerfilButton.visibility = View.GONE
        actualizarButton.visibility = View.VISIBLE
    }

    private fun editarPerfilPaso2() {
        nombreUsuarioEditText.visibility = View.GONE
        nombreUsuario.visibility = View.VISIBLE
        editarPerfilButton.visibility = View.VISIBLE
        actualizarButton.visibility = View.GONE

        val name = nombreUsuarioEditText.text.toString()
        val email = correoUsuario.text.toString()  // este es el que usas como clave en Firebase

        if (name.isNotEmpty() && email.isNotEmpty()) {
            // Actualizar en Firebase
            database.child("usuarios").child(email).child("nombre").setValue(name)
            // Actualizar en SharedPreferences
            sharedPreferences.edit().putString("name", name).apply()
        }

        actualizarDatos()
    }

    private fun actualizarDatos() {
        val name = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "") // sin .com

        nombreUsuario.text = name
        correoUsuario.text = email
        nombreUsuarioEditText.text = name

        // --- Mostrar u ocultar botón Admin según el correo guardado ---
        // Si te registraste con admin@gmail.com, aquí se guarda admin@gmail
        if (email == "admin@gmail") {
            adminButton.visibility = View.VISIBLE
        } else {
            adminButton.visibility = View.GONE
        }
    }
}
