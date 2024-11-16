package com.pedidos.android.persistence.model

import com.google.gson.annotations.SerializedName

data class SelectedTipoDocumento(
    @SerializedName("documentoidentidad")
    var codigo: Int,
    @SerializedName("descripciondocumentoidentidad")
    var description: String,
    @SerializedName("teclado")
    var teclado: String
)
