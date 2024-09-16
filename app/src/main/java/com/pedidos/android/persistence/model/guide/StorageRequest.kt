package com.pedidos.android.persistence.model.guide

import java.io.Serializable

class StorageRequest : Serializable {
    var empresa: String? = null
    var usuario: String? = null
    var almacen: String? = null
}