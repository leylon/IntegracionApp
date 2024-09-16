package com.pedidos.android.persistence.db.entity

import com.google.gson.annotations.SerializedName

class GeneratedDocumentEntity {

    @SerializedName("tipdoc")
    var documentType: String = ""

    @SerializedName("numdoc")
    var documentNumber: String = ""

    @SerializedName("tipdoctemp")
    var documentTypeTemp: String = ""

    @SerializedName("numdoctemp")
    var documentNumberTemp: String = ""


    @SerializedName("codclie")
    var clientCode: String = ""

    @SerializedName("nomclie")
    var clientName: String = ""

    @SerializedName("sbmone")
    var currencySimbol: String = ""

    @SerializedName("impdoc")
    var total: Double = 0.0
}