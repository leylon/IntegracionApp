package com.pedidos.android.persistence.db.entity

import com.google.gson.annotations.SerializedName

class OperationReportResponseEntity {
    @SerializedName("documentoprint")
    var documentToPrint: String = ""
}