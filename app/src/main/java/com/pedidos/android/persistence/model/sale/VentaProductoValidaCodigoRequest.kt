package com.pedidos.android.persistence.model.sale

import com.google.gson.annotations.SerializedName

/**
 *{
"CO_EMPR":"01",
"CO_TIEN":"K06",
"DE_CODI":"1234567788999999",
"DE_CODI_INGR":"12345677889999991"
}

 */

data class VentaProductoValidaCodigoRequest(
    @SerializedName("CO_EMPR") val coEmpr: String,
    @SerializedName("CO_TIEN") val coTien: String,
    @SerializedName("DE_CODI") val deCodi: String,
    @SerializedName("DE_CODI_INGR") val deCodiIngr: String,
    @SerializedName("OPCION") val opcion: String,
    )