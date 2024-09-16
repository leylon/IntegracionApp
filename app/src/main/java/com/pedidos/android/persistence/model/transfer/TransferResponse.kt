package com.pedidos.android.persistence.model.transfer

import java.io.Serializable

class TransferResponse: Serializable {
    var result: Boolean? = null
    var message: String? = null
    var data: List<TransferDataResponse>? = null
}