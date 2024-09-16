package com.pedidos.android.persistence.model.picking

import java.io.Serializable

class Detalle: Serializable {
    var cantidad: Int? =null
    var cantidadpickada: Int? =null
    var descripcion: String? =null
    var imei: String? =null
    var precio: Double? =null
    var producto: String? =null
    var secuencial: Int? =null
    var stimei: String? =null
    var ubicacion: String? =null
    var unidadmedida: String? =null
    var bulto: Int? = null
    var pesoUnitario: Double? = null
    var pesoTotal: Double? = null
}