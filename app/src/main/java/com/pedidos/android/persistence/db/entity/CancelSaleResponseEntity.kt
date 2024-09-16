package com.pedidos.android.persistence.db.entity

import com.google.gson.annotations.SerializedName

class CancelSaleResponseEntity {
    @SerializedName("mensaje")
    var message: String = ""
    
    @SerializedName("documentoprint")
    var documentToPrint = ""
}