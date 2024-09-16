package com.pedidos.android.persistence.model.transfer

class TransferFinishRequest {
    var usuario: String? = null
    var tienda:  String? = null
    var tipdoc: String? = null
    var numdoc: String? = null
    var detalle: List<TransferShowDetail>? = null
}