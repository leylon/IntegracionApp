package com.pedidos.android.persistence.db.entity

import com.pedidos.android.persistence.model.Receipt
import com.google.gson.annotations.SerializedName

class ReceiptEntity : Receipt {
    @SerializedName("documentoprint")
    override var documentoPrint: String = ""
    override var qrbase64 : String = ""
    var pdfBytes: ByteArray = byteArrayOf()
}