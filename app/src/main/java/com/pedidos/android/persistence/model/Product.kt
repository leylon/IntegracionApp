package com.pedidos.android.persistence.model

interface Product {
    var codigoVenta: String
    var codigo: String
    var descripcion: String
    var stimei: Boolean
    var cantidad: Int
    var precio: Double
    var monedaSimbolo: String
    var complementaryRowColor: String
    var stimei2: Boolean
}