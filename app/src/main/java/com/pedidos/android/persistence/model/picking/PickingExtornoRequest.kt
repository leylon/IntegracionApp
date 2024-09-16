package com.pedidos.android.persistence.model.picking

data class PickingExtornoRequest(
    val fecha: String,
    val pedido: String,
    val usuario: String
)