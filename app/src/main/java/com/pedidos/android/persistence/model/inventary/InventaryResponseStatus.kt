package com.pedidos.android.persistence.model.inventary

import java.io.Serializable

class InventaryResponseStatus : Serializable{
    var result: Boolean? = null
    var message: String? = null
    var idlista: Int? = null
    var documento: String? = null
    var fecha: String? =null
    var opcion: String? = null
    var data: List<InventaryStatusItem> = ArrayList()
}