package com.pedidos.android.persistence.model.guide

import java.io.Serializable

class GuideRequest: Serializable {
    var usuario : String? = null
    var empresa : String? = null
    var fecha : String? = null
    var tipodocumento : String? = null
    var almacen : String? = null
    var operacion : String? = null
    var guiaremision : String? = null
    var tipoauxiliar : String? = null
    var codigoauxiliar : String? = null
    var direccionpartida : String? = null
    var distritopartida : String? = null
    var tipodocidentidad : String? = null
    var numerodocidentidad : String? = null
    var direccionllegada : String? = null
    var distritollegada : String? = null
    var observacion : String? = null
    var modalidadtransporte : String? = null
    var tipodoctransportista : String? = null
    var numerodoctransportista : String? = null
    var nombretransportista : String? = null
    var direcciontransportista : String? = null
    var distritotransportista : String? = null
    var placavehiculo : String? = null
    var descripcionvehiculo : String? = null
    var tolvavehiculo : String? = null
    var tipodocconductor : String? = null
    var numerodocconductor : String? = null
    var direccionconductor : String? = null
    var distritoconductor : String? = null
    var licenciaconductor : String? = null
    var nombreconductor : String? = null
    var detalle: List<GuideDetail>? = null

}