package com.pedidos.android.persistence.model.picking

data class PrincipalPedidosPicking(
    val `data`: List<PedidosPicking>,
    val message: String,
    val result: Boolean
)