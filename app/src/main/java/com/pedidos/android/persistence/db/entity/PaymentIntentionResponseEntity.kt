package com.pedidos.android.persistence.db.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PaymentIntentionResponseEntity (
    @SerializedName(value = "msg", alternate = ["message"])
    var msg: String = "",
    var success: Boolean = false,

    @SerializedName("order_id")
    var orderId: String = "",

    @SerializedName("pago_id")
    var pagoId : String = "",

    var amount: Int = 0,
    var status: String = "CANCELED"

)
