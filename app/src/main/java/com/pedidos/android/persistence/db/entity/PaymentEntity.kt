package com.pedidos.android.persistence.db.entity

import com.pedidos.android.persistence.model.Payment
import com.google.gson.annotations.SerializedName

class PaymentEntity(
) : Payment {
    @SerializedName("tidocu")
    override var tipoDocumento: String = ""

    @SerializedName("nudocu")
    override var numeroDocumento: String = ""

    @SerializedName("imefec")
    override var montoEfectivo: Double = 0.0

    @SerializedName("cotarj")
    override var codigoTarjeta: String = ""

    @SerializedName("imtarj")
    override var montoTarjeta: Double = 0.0

    @SerializedName("dempos")
    override var mpos: String = ""

    @SerializedName("immpos")
    override var mposAmount: Double = 0.0

    @SerializedName("mposbean")
    override var mposTransaction: String = ""

    @SerializedName("vuelo")
    override var flight: String = ""


    @SerializedName("imefecMK")
    override var montoMakeAndWish: Double = 0.0

    @SerializedName("impotro")
    override var montoOtro: Double = 0.0

    @SerializedName("codotro")
    override var codigoOtro: String = ""

    override var numotro: String = ""

    override var impago_fpay: Double = 0.0
    override var idpago_fpay: String = ""
    override var impago_link: Double = 0.0
    override var idpago_link: String = ""
    override var numvale: String = ""
    override var impvale: Double = 0.0

}