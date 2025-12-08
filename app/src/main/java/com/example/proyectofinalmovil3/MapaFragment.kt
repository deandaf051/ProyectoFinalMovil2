package com.example.proyectofinalmovil3

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.proyectofinalmovil3.Evento.Evento
import com.example.proyectofinalmovil3.databinding.FragmentMapaBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import java.io.IOException

// Implementamos OnMapReadyCallback para poder interactuar con el mapa
class MapaFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapaBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private var googleMap: GoogleMap? = null
    private var eventoList = mutableListOf<Evento>()

    // Variable para guardar el estado del tipo de mapa
    private var esVistaSatelite = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos Firebase
        database = FirebaseDatabase.getInstance().reference

        // Obtenemos el fragmento del mapa y notificamos cuando esté listo
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Cargamos los eventos desde Firebase
        cargarEventosDesdeFirebase()

        // --- INICIO DE LA NUEVA LÓGICA ---
        // Configurar el OnClickListener para el Chip "Vista de Mapa"
        binding.chipVistaMapa.setOnClickListener {
            // Invertimos el estado actual
            esVistaSatelite = !esVistaSatelite
            // Llamamos a la función que actualiza el tipo de mapa
            actualizarTipoDeMapa()
        }
        // --- FIN DE LA NUEVA LÓGICA ---
    }

    // Este método se llama cuando el mapa está listo para ser usado
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // --- MODIFICADO: Establecemos el tipo de mapa inicial ---
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Mover la cámara a una ubicación inicial (ej. Ciudad de México)
        val ubicacionInicial = LatLng(19.4326, -99.1332)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionInicial, 10f))
    }

    // --- INICIO: NUEVA FUNCIÓN ---
    private fun actualizarTipoDeMapa() {
        if (googleMap == null) return // No hacer nada si el mapa no está listo

        if (esVistaSatelite) {
            // Cambiar a vista Híbrido (satélite + etiquetas)
            googleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            binding.chipVistaMapa.text = "Vista Normal" // Cambiamos el texto del chip
        } else {
            // Cambiar a vista Normal
            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            binding.chipVistaMapa.text = "Vista Satélite" // Cambiamos el texto del chip
        }
    }
    // --- FIN: NUEVA FUNCIÓN ---

    private fun cargarEventosDesdeFirebase() {
        val eventosRef = database.child("eventos")
        eventosRef.get().addOnSuccessListener { dataSnapshot ->
            eventoList.clear() // Limpiamos la lista antes de añadir nuevos eventos
            for (eventoSnapshot in dataSnapshot.children) {
                val evento = eventoSnapshot.getValue(Evento::class.java)
                if (evento != null) {
                    eventoList.add(evento)
                }
            }
            // Una vez cargados los datos, creamos las tarjetas en la UI
            crearTarjetasDeEventos()
        }.addOnFailureListener {
            Log.e("MapaFragment", "Error al cargar los eventos desde Firebase.", it)
        }
    }

    private fun crearTarjetasDeEventos() {
        // Limpiamos cualquier vista que pudiera haber en el contenedor
        binding.contenedorEventos.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        // Por cada evento en nuestra lista, creamos una tarjeta
        for (evento in eventoList) {
            // Inflamos el layout de la tarjeta que creamos (item_evento_mapa.xml)
            val tarjetaView = inflater.inflate(R.layout.item_evento_mapa, binding.contenedorEventos, false)

            // Obtenemos las referencias a los TextViews e ImageView de la tarjeta
            val titulo: TextView = tarjetaView.findViewById(R.id.txtTituloEvento)
            val lugar: TextView = tarjetaView.findViewById(R.id.txtLugarEvento)
            val fecha: TextView = tarjetaView.findViewById(R.id.txtFechaEvento)
            val icono: ImageView = tarjetaView.findViewById(R.id.iconoCategoria)

            // Asignamos los datos del evento a las vistas
            titulo.text = evento.titulo
            lugar.text = evento.lugar
            fecha.text = "${evento.dia} ${evento.mes}, ${evento.hora}"

            // Aquí puedes cambiar el icono según la categoría
            // (por ahora lo dejamos fijo)

            // --- LA PARTE MÁS IMPORTANTE: El OnClickListener ---
            tarjetaView.setOnClickListener {
                // Cuando se pulsa la tarjeta, llamamos a la función para poner el pin
                ubicarEventoEnMapa(evento.lugar)
            }

            // Añadimos la tarjeta ya creada y con datos al contenedor LinearLayout
            binding.contenedorEventos.addView(tarjetaView)
        }
    }

    private fun ubicarEventoEnMapa(lugar: String?) {
        if (lugar.isNullOrEmpty() || googleMap == null) {
            Log.e("MapaFragment", "El lugar está vacío o el mapa no está listo.")
            return
        }

        // Geocoder convierte una dirección de texto en coordenadas
        val geocoder = Geocoder(requireContext())
        try {
            // Obtenemos una lista de posibles ubicaciones (nos quedamos con la primera)
            val addressList = geocoder.getFromLocationName(lugar, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)

                // Limpiamos marcadores anteriores
                googleMap?.clear()
                // Añadimos un nuevo marcador en la ubicación del evento
                googleMap?.addMarker(MarkerOptions().position(latLng).title(lugar))
                // Movemos la cámara suavemente a la nueva ubicación y hacemos zoom
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            } else {
                Log.e("MapaFragment", "No se encontraron coordenadas para el lugar: $lugar")
            }
        } catch (e: IOException) {
            Log.e("MapaFragment", "Error del Geocoder", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpiar la referencia para evitar fugas de memoria
    }
}
