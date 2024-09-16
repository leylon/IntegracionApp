package com.pedidos.android.persistence.db.entity

import com.google.gson.annotations.SerializedName

class CashBalanceEntity {

    @SerializedName("usuario")
    var username: String = ""

    @SerializedName("fecha")
    var date: String = ""
}