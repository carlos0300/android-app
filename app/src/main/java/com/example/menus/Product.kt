package com.example.menus

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Product(
    var id:String = "",
    var codBarras:String = "",
    var nombre: String = "",
    var pCompra: Double = 0.0,
    var pVenta: Double = 0.0,
    var urlImage: String = "",
    var porc_ganancia: Double = 0.25,
    var categoria: String = "",

){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "codBarras" to codBarras,
            "nombre" to nombre,
            "pCompra" to pCompra,
            "pVenta" to pVenta,
            "urlImage" to urlImage,
            "porc_ganancia" to porc_ganancia,
            "categoria" to categoria,
        )
    }
}

