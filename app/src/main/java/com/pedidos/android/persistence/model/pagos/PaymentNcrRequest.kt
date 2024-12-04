package com.pedidos.android.persistence.model.pagos

import com.google.gson.annotations.SerializedName

data class PaymentNcrRequest(
    @SerializedName("CO_EMPR")
    val empresa: String,
    @SerializedName("CO_UNID")
    val tienda: String,
    @SerializedName("TI_DOCU")
    val tipoDoc: String,
    @SerializedName("NU_DOCU")
    val numDocu: String
)
