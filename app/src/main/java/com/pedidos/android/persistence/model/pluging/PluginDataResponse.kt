package com.pedidos.android.persistence.model.pluging

import java.io.Serializable

data class PluginDataResponse(
    val campos_albventacamposlibres: CamposAlbventacamposlibres,
    val campos_facturasventacamposlibres: CamposFacturasventacamposlibres,
    val cargosdtos: List<Cargosdto>,
    val cliente: String,
    val codmonedaiso: String,
    val codvendedor: Int,
    val ejecucionexterna: Ejecucionexterna,
    val fecha: String,
    val hora: String,
    val lineas: List<Linea>,
    val n: String,
    val numero: Int,
    val serie: String,
    val tarjeta: String,
    val tipodoc: String
) : Serializable