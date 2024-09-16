package com.pedidos.android.persistence.model.picking

import java.io.Serializable

class PickingExtornoTerminarResponse : Serializable {
    var `data`: List<PedidoHead>? = null
    var message: String? = null
    var result: Boolean? = null
}