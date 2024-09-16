package com.pedidos.android.persistence.db.entity

import com.google.gson.annotations.SerializedName

class ClientResponseEntity {
    @SerializedName("codigo")
    var documentNumber: String = ""

    @SerializedName("nombres")
    var fullName: String = ""

    @SerializedName("direccion")
    var address: String = ""

    @SerializedName("correo")
    var email: String = ""

    @SerializedName("tipodocumento")
    var identityDocument: Int = 0

    @SerializedName("telefono")
    var phone : String = ""
}