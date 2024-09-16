package com.pedidos.android.persistence.model.sale

import com.google.gson.annotations.SerializedName

data class EnvioCodigoResponse(
    @SerializedName("DE_RESU") var deResu: String
)