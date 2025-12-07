package com.example.proyectofinalmovil3

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class HomeFragment : Fragment(), SensorEventListener {

    private var txtCantidadPasos: TextView? = null

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    // valor inicial del contador del sistema (pasos desde que se encendió el cel)
    private var initialSteps: Float = -1f

    companion object {
        private const val REQUEST_ACTIVITY_RECOGNITION = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        txtCantidadPasos = view.findViewById(R.id.txtCantidadPasos)

        // SensorManager y sensor de pasos
        sensorManager =
            requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(
                requireContext(),
                "Tu dispositivo no tiene sensor de pasos",
                Toast.LENGTH_SHORT
            ).show()
            txtCantidadPasos?.text = "0"
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // cada vez que volvemos al fragment, registramos el listener (si hay permiso)
        if (stepSensor != null) {
            if (tienePermisoActividad()) {
                sensorManager.registerListener(
                    this,
                    stepSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            } else {
                pedirPermisoActividad()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // dejamos de escuchar el sensor para ahorrar batería
        if (stepSensor != null) {
            sensorManager.unregisterListener(this)
        }
    }

    // ===== SensorEventListener =====
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (initialSteps < 0f) {
                // primer valor: lo tomamos como base
                initialSteps = event.values[0]
            }
            val pasosActuales = event.values[0] - initialSteps
            txtCantidadPasos?.text = pasosActuales.toInt().toString()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no lo necesitamos por ahora
    }

    // ===== Permisos =====
    private fun tienePermisoActividad(): Boolean {
        // Antes de Android 10 no hace falta este permiso
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
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // Ya hay permiso, registramos el listener
                if (stepSensor != null) {
                    sensorManager.registerListener(
                        this,
                        stepSensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Sin permiso de actividad física no se pueden contar los pasos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
