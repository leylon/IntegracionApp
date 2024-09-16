package com.pedidos.android.persistence.model.pagos

import com.pedidos.android.persistence.model.inventary.InventaryStatusItem

class PagoValeResponse {
    var result: Boolean? = null
    var message: String? = null
    var data: PagoValeDataResponse? = null
}