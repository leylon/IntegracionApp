package com.pedidos.android.persistence.model.pagos

import com.google.gson.annotations.SerializedName

data class PaymentNcrResponse(
    @SerializedName("CO_EMPL")
    val coEmpl: String,
    @SerializedName("CO_EMPR")
    val coEmpr: String,
    @SerializedName("CO_MONE")
    val coMone: String,
    @SerializedName("CO_MOTI_DEVO")
    val coMotiDevo: String,
    @SerializedName("CO_UNID")
    val CO_UNID: String,
    @SerializedName("FE_DOCU")
    val FE_DOCU: String,
    @SerializedName("IM_DISP")
    val IM_DISP: Double,
    @SerializedName("NU_DIAS")
    val NU_DIAS: Int,
    @SerializedName("NU_DOCU")
    val NU_DOCU: String,
    @SerializedName("NU_RUCS")
    val NU_RUCS: String,
    @SerializedName("TI_DOCU")
    val TI_DOCU: String
)