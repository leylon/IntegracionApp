package com.pedidos.android.persistence.model.inventary

class InventaryGenerateRequest {
    var usuario: String? = null
    var fecha: String? = null
    var tienda: String? = null
    var idlista: Int? = null
    var documento: String? = null
    var opcion: String? = null
    var detalle: List<InventaryGenerateItem>? = null
}