package com.example.proyectofinalmovil3.Evento

import java.io.Serializable

data class Evento(
    // Añadimos valores por defecto a todos los campos
    val mes: String = "",
    val dia: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val inscritos: Int = 0,
    val hora: String = "",
    val lugar: String = ""
) : Serializable
