package com.pedidos.android.persistence.model

interface Payment {
    var tipoDocumento: String
    var numeroDocumento: String
    var montoEfectivo: Double
    var codigoTarjeta: String
    var montoTarjeta: Double
    var mpos: String
    var mposAmount: Double
    var mposTransaction: String
    var flight: String
    var montoMakeAndWish: Double
    var montoOtro : Double
    var codigoOtro : String
    var impago_fpay : Double
    var idpago_fpay : String
    var impago_link : Double
    var idpago_link : String
    var numotro: String
    var numvale: String
    var impvale: Double
}