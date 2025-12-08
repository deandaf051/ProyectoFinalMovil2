package com.example.proyectofinalmovil3

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log // --- AÑADIDO ---
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button // --- AÑADIDO ---
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView // --- AÑADIDO ---
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot // --- AÑADIDO ---
import com.google.firebase.database.DatabaseError // --- AÑADIDO ---
import com.google.firebase.database.DatabaseReference // --- AÑADIDO ---
import com.google.firebase.database.FirebaseDatabase // --- AÑADIDO ---
import com.google.firebase.database.ValueEventListener // --- AÑADIDO ---

class HomeFragment : Fragment(), SensorEventListener {

    // --- MODIFICADO: Declaración de todas las vistas necesarias ---
    private lateinit var txtCantidadPasos: TextView
    private lateinit var txtCantidadEventos: TextView
    private lateinit var txtNombreUsuario: TextView
    private lateinit var cardBuscarEventos: CardView
    private lateinit var cardMisEstadisticas: CardView

    // --- Variables para el sensor (tu código) ---
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var initialSteps: Float = -1f

    // --- AÑADIDO: Variables para Firebase ---
    private lateinit var database: DatabaseReference
    private var totalStepsFromDB: Int = 0 // Para guardar los pasos de la BD

    companion object {
        private const val REQUEST_ACTIVITY_RECOGNITION = 1001
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // --- MODIFICADO: Inicialización de todas las vistas ---
        txtCantidadPasos = view.findViewById(R.id.txtCantidadPasos)
        txtCantidadEventos = view.findViewById(R.id.txtCantidadEventos)
        txtNombreUsuario = view.findViewById(R.id.txtNombreUsuario)
        cardBuscarEventos = view.findViewById(R.id.cardBuscarEventos)
        cardMisEstadisticas = view.findViewById(R.id.cardMisEstadisticas)

        // --- AÑADIDO: Inicialización de Firebase ---
        database = FirebaseDatabase.getInstance().reference

        // --- MODIFICADO: Llamamos a la función principal que carga todo ---
        cargarDatosDelUsuario()

        // SensorManager y sensor de pasos (tu código)
        sensorManager =
            requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(
                requireContext(),
                "Tu dispositivo no tiene sensor de pasos",
                Toast.LENGTH_SHORT
            ).show()
        }

        return view
    }

    // --- AÑADIDO: Función principal para cargar datos desde Firebase ---
    private fun cargarDatosDelUsuario() {
        val emailKey = sharedPreferences.getString("email", null)

        if (emailKey == null) {
            Log.e("HomeFragment", "ERROR: No se encontró la clave 'email' en SharedPreferences.")
            // Valores por defecto si no hay sesión
            txtNombreUsuario.text = "Invitado"
            txtCantidadEventos.text = "0"
            txtCantidadPasos.text = "0"
            return
        }

        // Cargar el nombre de usuario
        database.child("usuarios").child(emailKey).child("nombre").get().addOnSuccessListener {
            if(it.exists()) {
                txtNombreUsuario.text = it.getValue(String::class.java)
            }
        }

        // Cargar los datos de las tarjetas (Eventos y Pasos)
        val statsRef = database.child("usuarios").child(emailKey).child("estadisticas")
        statsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val limpieza = snapshot.child("eventosLimpieza").getValue(Int::class.java) ?: 0
                    val reciclaje = snapshot.child("eventosReciclaje").getValue(Int::class.java) ?: 0
                    val reforestacion = snapshot.child("eventosReforestacion").getValue(Int::class.java) ?: 0
                    totalStepsFromDB = snapshot.child("pasosTotales").getValue(Int::class.java) ?: 0

                    val eventosTotales = limpieza + reciclaje + reforestacion

                    txtCantidadEventos.text = eventosTotales.toString()
                    txtCantidadPasos.text = totalStepsFromDB.toString()
                } else {
                    txtCantidadEventos.text = "0"
                    txtCantidadPasos.text = "0"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Error al cargar las estadísticas.", error.toException())
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (stepSensor != null) {
            if (tienePermisoActividad()) {
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            } else {
                pedirPermisoActividad()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (stepSensor != null) {
            sensorManager.unregisterListener(this)
        }
        // --- AÑADIDO: Guardar los pasos al pausar el fragmento ---
        guardarPasosEnFirebase()
    }

    // --- MODIFICADO: onSensorChanged para usar los pasos de la BD como base ---
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (initialSteps < 0f) {
                initialSteps = event.values[0]
            }
            // Pasos dados desde que se abrió el fragment
            val pasosNuevos = event.values[0] - initialSteps
            // Total a mostrar = Pasos de la BD + nuevos pasos
            val pasosTotalesAMostrar = totalStepsFromDB + pasosNuevos.toInt()
            txtCantidadPasos.text = pasosTotalesAMostrar.toString()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no lo necesitamos por ahora
    }

    // --- AÑADIDO: Nuevo método para guardar los pasos ---
    private fun guardarPasosEnFirebase() {
        val emailKey = sharedPreferences.getString("email", null) ?: return

        // Tomamos el último valor mostrado en pantalla y lo guardamos
        val pasosFinales = txtCantidadPasos.text.toString().toIntOrNull() ?: totalStepsFromDB
        database.child("usuarios").child(emailKey).child("estadisticas").child("pasosTotales").setValue(pasosFinales)
            .addOnSuccessListener {
                Log.d("HomeFragment", "Pasos ($pasosFinales) guardados en Firebase.")
            }
    }


    // ===== Permisos (Tu código original, está perfecto) =====
    private fun tienePermisoActividad(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun pedirPermisoActividad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_ACTIVITY_RECOGNITION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (stepSensor != null) {
                    sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
                }
            } else {
                Toast.makeText(requireContext(), "Sin permiso no se pueden contar pasos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
