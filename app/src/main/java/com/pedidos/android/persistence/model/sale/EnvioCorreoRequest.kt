package com.pedidos.android.persistence.model.sale

import com.google.gson.annotations.SerializedName


data class EnvioCorreoRequest(
    @SerializedName("CO_TIEN") val coTien: String,
    @SerializedName("DE_CODI") val deCodi: String,
    @SerializedName("FE_CODI") val feCodi: String,
    @SerializedName("DE_MAIL") val deMail: String,
    @SerializedName("TI_DOCU_IDEN") val tiDocuIden: String,
    @SerializedName("NU_DOCU_IDEN") val nuDocuIden: String,
    @SerializedName("NO_CLIE") val noClie: String,
    @SerializedName("CO_VEND") val coVend: String,
    @SerializedName("NO_VEND") val noVend: String,
    @SerializedName("Detalle") val detalle: List<VentaProductoRequest>,
)
