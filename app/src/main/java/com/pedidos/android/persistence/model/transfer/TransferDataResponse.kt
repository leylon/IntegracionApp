package com.pedidos.android.persistence.model.transfer

import java.io.Serializable

class TransferDataResponse: Serializable {
    var numdoc: String? = null
    var tipdoc: String? = null
    var fecha: String? = null
    var tienda: String? = null
    var estado: String? = null
    var observacion: String? = null
    var detalle: List<TransferShowDetail>? = null

}