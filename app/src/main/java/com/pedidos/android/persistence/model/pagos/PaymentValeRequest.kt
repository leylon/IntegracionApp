package com.pedidos.android.persistence.model.pagos

import com.google.gson.annotations.SerializedName

data class PaymentValeRequest(
    val usuario: String,
    @SerializedName("CO_TIEN")
    val tienda: String,
    @SerializedName("NU_VALE")
    val vale: String
)