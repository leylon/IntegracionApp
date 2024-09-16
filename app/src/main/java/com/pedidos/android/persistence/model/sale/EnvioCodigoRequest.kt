package com.pedidos.android.persistence.model.sale

import com.google.gson.annotations.SerializedName


data class EnvioCodigoRequest(
    @SerializedName("CO_EMPR") val coEmpr: String,
    @SerializedName("DE_CODI") val deCodi: String,
)
