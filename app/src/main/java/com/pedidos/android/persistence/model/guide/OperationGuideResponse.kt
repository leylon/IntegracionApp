package com.pedidos.android.persistence.model.guide

import java.io.Serializable

class OperationGuideResponse: Serializable {
    var codigo: String? = null
    var descripcion: String? = null
    var direccionpartida: String? = null
    var ubigeopartida: String? = null
    var tipoauxiliar: String? = null
    var auxiliar: String? = null
    var tipodocuiden: String? = null
    var docuiden: String? = null
    var direccionllegada: String? = null
    var ubigeollegada: String? = null

}