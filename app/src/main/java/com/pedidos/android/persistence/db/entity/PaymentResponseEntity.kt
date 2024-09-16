package com.pedidos.android.persistence.db.entity

import android.arch.persistence.room.Ignore
import com.google.gson.annotations.SerializedName

class PaymentResponseEntity {
    @SerializedName("tidocu")
    var tipoDocumento: String = ""

    @SerializedName("nudocu")
    var numeroDocumento: String = ""

    @SerializedName("sbmone")
    var moneda: String = ""

    @SerializedName("imtota")
    var total: Double = 0.0

    @SerializedName("impaga")
    var montoPagado: Double = 0.0

    @SerializedName("imcamb")
    var cambio: Double = 0.0

    @SerializedName("documentoprint")
    var documentoPrint : String = ""

    @SerializedName("piedocumentoprint")
    var piedocumentoPrint : String = ""

    @SerializedName("imagenqr")
    var qrPrint : String = ""

    @SerializedName("voucherprint")
    var voucherMposPrint : String = ""


    @Ignore()
    var serviceResultMessage : String = ""
}