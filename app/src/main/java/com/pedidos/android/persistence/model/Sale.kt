package com.pedidos.android.persistence.model

import android.os.Parcelable

interface Sale : Parcelable {
    var documento: String
    var fecha: String
    var clienteTipoDocumento: Int
    var clienteCodigo: String
    var clienteNombres: String
    var usuario: String
    var vendedorCodigo: String
    var cajaCodigo: String
    var tienda: String
    var subTotal: Double
    var descuento: Double
    var impuesto: Double
    var total: Double
    var evento: String
    var monedaSimbolo: String
    var complementaryRowColor: String
    var impuesto2 : Double
    var impuesto3 : Double
    var nombreimpuesto1 : String
    var nombreimpuesto2 : String
    var nombreimpuesto3 : String
    var productoconcomplemento : Int
    var telefono : String
    var email : String
    var androidimei: String
}