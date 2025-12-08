package com.example.proyectofinalmovil3.Evento

import java.io.Serializable

data class Evento(
    val mes: String,
    val dia: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val inscritos: Int,
    val hora: String,
    val lugar: String
) : Serializable
