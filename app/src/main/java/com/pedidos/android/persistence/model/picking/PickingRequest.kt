package com.pedidos.android.persistence.model.picking

import com.google.gson.annotations.SerializedName

data class PickingRequest(
    val fecha: String,
    val pedido: String,
    val producto: String,
    val secuencial: Int,
    val signo: String,
    val usuario: String,
    @SerializedName("pesoTotal")
    val pesoTotal: Double,
    @SerializedName("pesoUnitario")
    val pesoUnitario: Double
)