package com.pedidos.android.persistence.model

import android.os.Parcelable

interface SaleSubItem: Parcelable {
    var secuencial: Int
    var codigoventa: String
    var codigoProducto: String
    var cantidad: Int
    var descripcion: String
    var imei: String
    var imei2: String
    var precio: Double
    var pcdcto: Double
    var imdcto: Double
    var monedaSimbolo: String


    var secgaraexte : Int
    var codgaraexte : String
    var stimei: Boolean
    /// FK Sale
    var saleCodigo: String
    var complementaryRowColor: String
    var productoconcomplemento : Int

    //Picking
    var pesoTotal: Double
    var pesoUnitario: Double
    var cantidadpickada: Int
}
