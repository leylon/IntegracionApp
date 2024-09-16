package com.pedidos.android.persistence.db.entity

import android.os.Parcel
import android.os.Parcelable
import com.pedidos.android.persistence.model.Product
import com.google.gson.annotations.SerializedName

data class ProductEntity(override var codigoVenta: String = "",
                         override var codigo: String = "",
                         override var descripcion: String = "",
                         override var stimei: Boolean = false,
                         override var cantidad: Int = 0,
                         override var precio: Double = 0.0,
                         @SerializedName("sbmone")
                         override var monedaSimbolo: String = "",
                         @SerializedName("colordetalle")
                         override var complementaryRowColor: String = "",
                         @SerializedName("stimei2")
                         override var stimei2: Boolean = false) : Product, Parcelable {

    var imei: String = ""
    var imei2: String = ""
    var secgaraexte : Int = 0
    var codgaraexte : String = ""

    constructor(parcel: Parcel) : this() {
        codigoVenta = parcel.readString()
        codigo = parcel.readString()
        descripcion = parcel.readString()
        stimei = parcel.readByte() != 0.toByte()
        cantidad = parcel.readInt()
        precio = parcel.readDouble()
        imei = parcel.readString()
        imei2 = parcel.readString()
        monedaSimbolo = parcel.readString()
        complementaryRowColor = parcel.readString()
        secgaraexte = parcel.readInt()
        codgaraexte = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(codigoVenta)
        parcel.writeString(codigo)
        parcel.writeString(descripcion)
        parcel.writeByte(if (stimei) 1 else 0)
        parcel.writeInt(cantidad)
        parcel.writeDouble(precio)
        parcel.writeString(imei)
        parcel.writeString(imei2)
        parcel.writeString(monedaSimbolo)
        parcel.writeString(complementaryRowColor)
        parcel.writeInt(secgaraexte)
        parcel.writeString(codgaraexte)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductEntity> {
        override fun createFromParcel(parcel: Parcel): ProductEntity {
            return ProductEntity(parcel)
        }

        override fun newArray(size: Int): Array<ProductEntity?> {
            return arrayOfNulls(size)
        }
    }
}