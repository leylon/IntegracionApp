package com.pedidos.android.persistence.model.sale

import java.io.Serializable

data class VentaProductoResponse (
    var result: Boolean,
    var muestramensaje: Boolean,
    var mensaje: String,
    var correo: String,
    var codigoautorizacion: String,
    var tienda: String,
    var bloqueafotocheck: String
) : Serializable
