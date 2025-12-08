package com.example.proyectofinalmovil3

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.alpha
import androidx.fragment.app.Fragment
import com.example.proyectofinalmovil3.databinding.FragmentStatsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StatsFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var totalSteps = 0 // Pasos totales leídos de Firebase
    private var initialSteps = -1 // Pasos iniciales del sensor al abrir el fragmento

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference

        // --- CORRECCIÓN 1: Usar el nombre correcto de SharedPreferences ---
        // Tu MainActivity usa "MyPrefs", así que aquí también debemos usar "MyPrefs".
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            // El dispositivo no tiene sensor de pasos, podrías mostrar un mensaje.
            Log.e("StatsFragment", "Este dispositivo no tiene sensor de contador de pasos.")
        }
        cargarEstadisticas()
    }

    override fun onResume() {
        super.onResume()
        // Registramos el listener del sensor cuando el fragmento está visible.
        stepCounterSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // Dejamos de escuchar al sensor cuando el fragmento ya no está visible.
        sensorManager?.unregisterListener(this)

        // Guardamos los pasos acumulados en Firebase al salir del fragmento
        guardarPasosEnFirebase()
    }

    private fun cargarEstadisticas() {
        val emailKey = sharedPreferences.getString("email", null)

        if (emailKey == null) {
            Log.e("StatsFragment", "ERROR: No se encontró la clave 'email' en SharedPreferences.")
            actualizarUIConDatos(0, 0, 0, 0, 0)
            return
        }

        Log.d("StatsFragment", "Clave leída de SharedPreferences para la búsqueda: '$emailKey'")

        val statsRef = database.child("usuarios").child(emailKey).child("estadisticas")
        Log.d("StatsFragment", "Ruta de búsqueda en Firebase: ${statsRef.toString()}")

        statsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d("StatsFragment", "¡Datos encontrados! Contenido: ${snapshot.value}")

                    // Leemos los valores individuales de cada tipo de evento
                    val limpieza = snapshot.child("eventosLimpieza").getValue(Int::class.java) ?: 0
                    val reciclaje = snapshot.child("eventosReciclaje").getValue(Int::class.java) ?: 0
                    val reforestacion = snapshot.child("eventosReforestacion").getValue(Int::class.java) ?: 0

                    // --- MODIFICACIÓN CLAVE PARA EL CONTADOR DE PASOS ---
                    // Guardamos el valor de la base de datos en nuestra variable de clase 'totalSteps'.
                    // Esto establece el punto de partida antes de que el sensor comience a sumar.
                    totalSteps = snapshot.child("pasosTotales").getValue(Int::class.java) ?: 0

                    // Calculamos 'eventosAsistidos' como la suma de los otros tres.
                    val eventosAsistidos = limpieza + reciclaje + reforestacion

                    // Sobrescribimos el campo 'eventosAsistidos' en la base de datos con el total calculado.
                    statsRef.child("eventosAsistidos").setValue(eventosAsistidos)
                        .addOnSuccessListener {
                            Log.d("StatsFragment", "Campo 'eventosAsistidos' actualizado en Firebase con el valor: $eventosAsistidos")
                        }
                        .addOnFailureListener { e ->
                            Log.e("StatsFragment", "Error al actualizar 'eventosAsistidos' en Firebase", e)
                        }

                    // Pasamos los valores iniciales a la función que actualiza la UI.
                    // 'totalSteps' se usa aquí para mostrar el valor inicial mientras el sensor se activa.
                    actualizarUIConDatos(eventosAsistidos, totalSteps, limpieza, reciclaje, reforestacion)

                } else {
                    Log.e("StatsFragment", "¡FALLO! No se encontró el nodo 'estadisticas' para la clave: '$emailKey'")
                    actualizarUIConDatos(0, 0, 0, 0, 0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StatsFragment", "Error al leer la base de datos", error.toException())
                actualizarUIConDatos(0, 0, 0, 0, 0)
            }
        })
    }


    private fun actualizarUIConDatos(
        eventosAsistidos: Int,
        pasosTotales: Int,
        limpieza: Int,
        reciclaje: Int,
        reforestacion: Int
    ) {
        binding.txtNumEventos.text = eventosAsistidos.toString()
        binding.txtNumPasos.text = pasosTotales.toString()
        binding.txtNumLimpieza.text = limpieza.toString()
        binding.txtNumReciclaje.text = reciclaje.toString()
        binding.txtNumReforestacion.text = reforestacion.toString()

        actualizarLogros(eventosAsistidos)
    }

    private fun guardarPasosEnFirebase() {
        val emailKey = sharedPreferences.getString("email", null) ?: return

        if (initialSteps != -1) {
            // Calculamos el valor final a guardar
            val currentSensorSteps = (binding.txtNumPasos.text.toString().toIntOrNull() ?: totalSteps)
            val statsRef = database.child("usuarios").child(emailKey).child("estadisticas")
            statsRef.child("pasosTotales").setValue(currentSensorSteps)
                .addOnSuccessListener {
                    Log.d("StatsFragment", "Pasos totales ($currentSensorSteps) guardados en Firebase.")
                }
        }
    }

    private fun actualizarLogros(eventosAsistidos: Int) {
        // Logro 1: Primeros Pasos (se desbloquea con 5 eventos)
        if (eventosAsistidos >= 5) {
            // Logro DESBLOQUEADO
            binding.logroPrimerosPasos.alpha = 1.0f // Hacemos toda la fila visible
            binding.imgLogroPrimerosPasos.alpha = 1.0f // Nos aseguramos que el ícono sea 100% visible
            // Cambiamos el color del trofeo a dorado
            binding.imgLogroPrimerosPasos.setColorFilter(
                requireContext().getColor(R.color.colorGold)
            )
        } else {
            // Logro BLOQUEADO
            binding.logroPrimerosPasos.alpha = 0.5f // Hacemos la fila semitransparente
            // Revertimos cualquier filtro de color para que vuelva a su estado original (gris)
            binding.imgLogroPrimerosPasos.clearColorFilter()
        }

        // Logro 2: Guerrero Ecológico (se desbloquea con 10 eventos)
        if (eventosAsistidos >= 10) {
            // Logro DESBLOQUEADO
            binding.logroGuerreroEcologico.alpha = 1.0f
            binding.imgLogroGuerreroEcologico.alpha = 1.0f
            // Cambiamos el color del trofeo a dorado
            binding.imgLogroGuerreroEcologico.setColorFilter(
                requireContext().getColor(R.color.colorGold)
            )
        } else {
            // Logro BLOQUEADO
            binding.logroGuerreroEcologico.alpha = 0.5f
            // Revertimos el filtro de color
            binding.imgLogroGuerreroEcologico.clearColorFilter()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //Nada
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val currentSteps = it.values[0].toInt()

                if (initialSteps == -1) {
                    // La primera vez que recibimos un evento del sensor,
                    // guardamos el valor actual del sensor como punto de partida.
                    initialSteps = currentSteps
                }

                // Calculamos los nuevos pasos dados desde que se abrió el fragmento
                val newSteps = currentSteps - initialSteps

                // Actualizamos la UI sumando los pasos de la BD + los nuevos pasos
                binding.txtNumPasos.text = (totalSteps + newSteps).toString()
            }
        }
    }
}
