package com.pedidos.android.persistence.model.picking

import java.io.Serializable

class PickingTerminarResponse : Serializable {
    var `data`: PedidoHead? = null
    var message: String? = null
    var result: Boolean? = null
}