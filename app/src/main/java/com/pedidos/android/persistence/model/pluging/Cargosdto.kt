package com.pedidos.android.persistence.model.pluging

import java.io.Serializable

data class Cargosdto(
    val codigo: Int,
    val nombre: String,
    val valor: Int
): Serializable