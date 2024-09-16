package com.pedidos.android.persistence.model.picking

import com.pedidos.android.persistence.model.SaleSubItem

data class PickingTerminarRequest(
    val fecha: String,
    val pedido: String,
    val ubicacion: String,
    val usuario: String,
    val bulto: Int,
    val data: List<SaleSubItem>
)