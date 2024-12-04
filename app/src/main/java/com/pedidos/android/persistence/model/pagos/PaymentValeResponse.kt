package com.pedidos.android.persistence.model.pagos

import com.google.gson.annotations.SerializedName

data class PaymentValeResponse(
    @SerializedName("respuesta")
    val result: Boolean,
    @SerializedName("mensaje")
    val message: String,
    @SerializedName("NU_VALE")
    val vale: String,
    @SerializedName("NU_BARR")
    val barra: String,
    @SerializedName("IM_VALE")
    val importe: Double,
    @SerializedName("FE_VENC")
    val fechaVencimiento: String,
)
