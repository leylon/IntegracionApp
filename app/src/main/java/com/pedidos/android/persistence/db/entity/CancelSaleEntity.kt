package com.pedidos.android.persistence.db.entity

import com.google.gson.annotations.SerializedName

class CancelSaleEntity {
    @SerializedName("pedido")
    var numeroDocumento: String = ""

    @SerializedName("documento")
    var tipoDocumento: String = ""

    @SerializedName("nrotran")
    var numeroTransaccion: String = ""

    @SerializedName("mposbean")
    var mposBean: String = ""
}