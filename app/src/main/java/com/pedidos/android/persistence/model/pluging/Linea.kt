package com.pedidos.android.persistence.model.pluging

import com.pedidos.android.persistence.db.entity.ProductEntity

data class Linea(
    val almacen: String,
    val cargo1: Int,
    val cargo2: Int,
    val color: String,
    val dto: Double,
    val numlinea: Int,
    val preciobruto: Double,
    val precioiva: Double,
    val precioneto: Double,
    val referencia: String,
    val talla: String,
    val unidades: Int,
    val imei: String
) : java.io.Serializable