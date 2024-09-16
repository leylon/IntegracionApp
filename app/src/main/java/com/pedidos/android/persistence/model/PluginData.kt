package com.pedidos.android.persistence.model

data class PluginData(
    val campos_pedventacamposlibres: CamposPedventacamposlibres,
    val codvendedor: Int,
    val datosentrega: Datosentrega,
    val ejecucionexterna: Ejecucionexterna
)