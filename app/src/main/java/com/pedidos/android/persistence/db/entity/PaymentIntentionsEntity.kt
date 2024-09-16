package com.pedidos.android.persistence.db.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PaymentIntentionsEntity (
    @SerializedName("tienda")
    var market : String = "",
    var amount : Float = 0f,
    var email : String = "",
    var pedido : String = ""
    ) : Serializable