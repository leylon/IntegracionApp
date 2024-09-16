package com.pedidos.android.persistence.model.sale

import com.google.gson.annotations.SerializedName

data class VentaProductoRequest(
    @SerializedName("CO_EMPR") val coEmpr: String,
    @SerializedName("NU_SECU") val nuSecu: Int,
    @SerializedName("CO_ITEM") val coItem: String,
    @SerializedName("CO_VENT") val coVent: String,
    @SerializedName("DE_ITEM") val deItem: String,
    @SerializedName("CA_DOCU") val caDocu: Int,
    @SerializedName("PR_VENT") val prVent: Double,
)
