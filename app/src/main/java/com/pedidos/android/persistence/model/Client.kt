package com.pedidos.android.persistence.model

interface Client {
    var identityDocumentType : Int
    var documentNumber: String
    var fullName: String
    var address: String
    var email: String
    var phone : String
}