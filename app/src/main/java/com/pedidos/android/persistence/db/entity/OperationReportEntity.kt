package com.pedidos.android.persistence.db.entity

import com.google.gson.annotations.SerializedName

class OperationReportEntity {
    @SerializedName("pedido")
    var numeroDocumento: String = ""

    @SerializedName("documento")
    var tipoDocumento: String = ""

    @SerializedName("mposbean")
    var mposBean: String = ""

}