package com.pedidos.android.persistence.model.picking

data class ReportPickingResponse(
    val result: Boolean,
    val message: String,
    val data: List<ReportPickingDet>
)
