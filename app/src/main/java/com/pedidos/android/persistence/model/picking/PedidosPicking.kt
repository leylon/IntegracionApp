package com.pedidos.android.persistence.model.picking

data class PedidosPicking(
    val estado: String,
    val fecha: String,
    val moneda: String,
    val pedido: String,
    val simbolo: String,
    val tienda: String,
    val tipdoc: String,
    val total: Double
)