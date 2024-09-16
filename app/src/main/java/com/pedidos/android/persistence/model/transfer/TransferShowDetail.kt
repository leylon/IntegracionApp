package com.pedidos.android.persistence.model.transfer

import java.io.Serializable

class TransferShowDetail: Serializable {
    var secuencial: Int? = null
    var producto: String? = null
    var descripcion: String? = null
    var imei: String? = null
    var cantidad: Int? = null
    var cantidadPickada: Int? = null
    var pesoUnitario: Double? = null
    var pesoTotal: Double? = null
}