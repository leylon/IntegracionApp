package com.pedidos.android.persistence.db.entity

import android.os.DeadObjectException
import android.os.Parcel
import android.os.Parcelable
import com.pedidos.android.persistence.model.SaleSubItem
import com.google.gson.annotations.SerializedName

class SaleSubItemEntity() : SaleSubItem {
    override var secuencial: Int = 1

    @SerializedName("codigoproducto")
    override var codigoProducto: String = ""
    override var pcdcto: Double = 0.0
    override var imdcto: Double = 0.0
    override var saleCodigo: String = ""

    @SerializedName("codigoventa")
    override var codigoventa: String = ""
    override var descripcion: String = ""
    override var imei: String = ""
    override var imei2: String = ""
    override var cantidad: Int = 0
    override var precio: Double = 0.0

    @SerializedName("sbmone")
    override var monedaSimbolo: String = ""
    @SerializedName("colordetalle")
    override var complementaryRowColor: String = ""
    override var stimei: Boolean = false

    override var secgaraexte : Int = 0
    override var codgaraexte : String = ""
    override var productoconcomplemento: Int = 0
    override var pesoTotal: Double = 0.0
    override var pesoUnitario: Double = 0.0
    override var cantidadpickada: Int = 0

    constructor(parcel: Parcel) : this() {
        secuencial = parcel.readInt()
        codigoProducto = parcel.readString()
        pcdcto = parcel.readDouble()
        imdcto = parcel.readDouble()
        saleCodigo = parcel.readString()
        codigoventa = parcel.readString()
        descripcion = parcel.readString()
        imei = parcel.readString()
        imei2 = parcel.readString()
        cantidad = parcel.readInt()
        precio = parcel.readDouble()
        monedaSimbolo = parcel.readString()
        secgaraexte = parcel.readInt()
        codgaraexte = parcel.readString()
        productoconcomplemento = parcel.readInt()
        stimei = parcel.readInt() != 0
        pesoTotal = parcel.readDouble()
        pesoUnitario = parcel.readDouble()
        cantidadpickada = parcel.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(secuencial)
        dest.writeString(codigoProducto)
        dest.writeDouble(pcdcto)
        dest.writeDouble(imdcto)
        dest.writeString(saleCodigo)
        dest.writeString(codigoventa)
        dest.writeString(descripcion)
        dest.writeString(imei)
        dest.writeString(imei2)
        dest.writeInt(cantidad)
        dest.writeDouble(precio)
        dest.writeString(monedaSimbolo)
        dest.writeInt(secgaraexte)
        dest.writeString(codgaraexte)
        dest.writeInt(productoconcomplemento)
        if (stimei){
            dest.writeInt(1)
        }else{
            dest.writeInt(0)
        }
        dest.writeDouble(pesoTotal)
        dest.writeDouble(pesoUnitario)
        dest.writeInt(cantidadpickada)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SaleSubItem> {
        override fun createFromParcel(parcel: Parcel): SaleSubItem {
            return SaleSubItemEntity(parcel)
        }

        override fun newArray(size: Int): Array<SaleSubItem?> {
            return arrayOfNulls(size)
        }
    }
}