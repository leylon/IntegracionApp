package com.pedidos.android.persistence.model

import com.google.gson.annotations.SerializedName

class LoginResponse(var cajacodigo: String,
                    var cajanombre: String,
                    var vendedorcodigo: String,
                    var vendedornombre: String,
                    var tienda: String,
                    var usuario: String,
                    var dutyfree: Int,
                    var telefono : String,
                    var email : String,
                    var empresa: String,
                    var imei: String,
                    var urlcorreo: String,
                    var urlcorreorespuesta: String,
                    @SerializedName("MakeaWish")
                    var makeaWish: Boolean,
                    @SerializedName("Efectivo")
                    var efectivo: Boolean,
                    @SerializedName("EfectivoDol")
                    var efectivoDol: Boolean,
                    @SerializedName("Tarjeta")
                    var tarjeta: Boolean,
                    @SerializedName("MPos")
                    var mPos: Boolean,
                    @SerializedName("OtroPago")
                    var otroPago: Boolean,
                    @SerializedName("Vales")
                    var vales: Boolean,
                    @SerializedName("PagoLink")
                    var pagoLink: Boolean,
                    @SerializedName("Fpay")
                    var fpay: Boolean,
                    @SerializedName("urlvale")
                    var urlvale: String,
                    @SerializedName("urlaplNCR")
                    var urlaplncr: String,
                    @SerializedName("AplNCR")
                    var aplncr: Boolean,
                    )