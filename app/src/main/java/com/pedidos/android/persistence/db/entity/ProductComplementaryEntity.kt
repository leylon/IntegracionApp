package com.pedidos.android.persistence.db.entity

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class ProductComplementaryEntity() : Parcelable {

    @SerializedName("codigocomplemento")
    var codigo: String = ""

    @SerializedName("idTipo")
    var type: Int = -1

    @SerializedName("descripcioncomplemento")
    var descripcion: String = ""

    @SerializedName("sbmone")
    var monedaSimbolo: String = ""

    @SerializedName("preciocomplemento")
    var precio: Double = 0.0

    @SerializedName("colorcomplemento")
    var rowColor: String = ""

    var checkStatus : Int = 0

    constructor(parcel: Parcel) : this() {
        codigo = parcel.readString()
        type = parcel.readInt()
        descripcion = parcel.readString()
        monedaSimbolo = parcel.readString()
        precio = parcel.readDouble()
        rowColor = parcel.readString()
        checkStatus = parcel.readInt()
    }

    companion object CREATOR : Parcelable.Creator<ProductComplementaryEntity> {
        override fun createFromParcel(parcel: Parcel): ProductComplementaryEntity {
            return ProductComplementaryEntity(parcel)
        }

        override fun newArray(size: Int): Array<ProductComplementaryEntity?> {
            return arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(codigo)
        parcel.writeString(descripcion)
        parcel.writeString(monedaSimbolo)
        parcel.writeDouble(precio)
        parcel.writeString(rowColor)
        parcel.writeInt(checkStatus)
        parcel.writeInt(type)
    }

    override fun describeContents(): Int {
        return 0
    }
}