package com.pedidos.android.persistence.model

import com.google.gson.annotations.SerializedName

data class CheckImeiResponse(
        @SerializedName("documentNumber") val codigo: String,
        @SerializedName("descripcion") val descripcion: String,
        @SerializedName("cantidad") val cantidad: Int
)