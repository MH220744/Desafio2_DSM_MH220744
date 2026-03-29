package com.example.viajes_desafio2dsm_mh220744

data class Destino(
    var id: String = "",
    var nombre: String = "",
    var pais: String = "",
    var precio: Double = 0.0,
    var descripcion: String = "",
    var imagenBase64: String = "",
    var userId: String = ""
)