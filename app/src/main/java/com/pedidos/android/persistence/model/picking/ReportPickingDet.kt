package com.pedidos.android.persistence.model.picking

data class ReportPickingDet(
    val color: Int,
    val direccion_cliente: String,
    val estado: String,
    val fecha: String,
    val mail_cliente: String,
    val moneda: String,
    val nombre: String,
    val num_doc_cliente: String,
    val observacion: String,
    val pedido: String,
    val peso: Double,
    val simbolo: String,
    val telefono_cliente: String,
    val tienda: String,
    val tip_doc_cliente: String,
    val tipdoc: String,
    val total: Double
)