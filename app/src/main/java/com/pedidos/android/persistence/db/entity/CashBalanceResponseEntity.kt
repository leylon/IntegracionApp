package com.pedidos.android.persistence.db.entity

import com.google.gson.annotations.SerializedName

class CashBalanceResponseEntity {
    var message: String = ""


    @SerializedName("documentoprint")
    var result: String = ""
}