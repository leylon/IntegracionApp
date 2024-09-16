package com.pedidos.android.persistence.model

import android.os.Parcelable

interface Settings : Parcelable {
    var urlbase: String
    var logoUrl: String
    var impresora: String
}