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
                    var makeaWish: String,
                    @SerializedName("Efectivo")
                    var efectivo: String,
                    @SerializedName("EfectivoDol")
                    var efectivoDol: String,
                    @SerializedName("Tarjeta")
                    var tarjeta: String,
                    @SerializedName("MPos")
                    var mPos: String,
                    @SerializedName("OtroPago")
                    var otroPago: String,
                    @SerializedName("Vales")
                    var vales: String,
                    @SerializedName("PagoLink")
                    var pagoLink: String,
                    @SerializedName("Fpay")
                    var fpay: String
                    )