package com.pedidos.android.persistence.model.picking

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PedidoHead : Serializable {
     var detalle: List<Detalle>? = null
     var estado: String? = null
     var fecha: String? = null
     var moneda: String? = null
     var pedido: String? = null
     var simbolo: String? = null
     var tienda: String? = null
     var tipdoc: String? = null
     var total: Double? = null
     var nombre: String? = null
     @SerializedName("tip_doc_cliente")
     var tipDocCliente: String? = null
     @SerializedName("num_doc_cliente")
     var numDocClient: String? = null
     @SerializedName("telefono_cliente")
     var telefonoCliente: String? = null
     @SerializedName("direccion_cliente")
     var direccionCliente: String? = null
     @SerializedName("mail_cliente")
     var mailCliente: String? =null
     var peso: Double? = null

 }