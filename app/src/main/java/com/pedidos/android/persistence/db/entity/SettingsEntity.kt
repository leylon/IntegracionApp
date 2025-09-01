package com.pedidos.android.persistence.db.entity

import android.os.Parcel
import android.os.Parcelable
import com.pedidos.android.persistence.model.Settings

class SettingsEntity() : Settings {
    override var urlbase: String = ""
    override var logoUrl: String = ""
    override var impresora: String = "Printer_"
    override var isLog: Int = 0
    override var pageSize: String = "80mm"

    constructor(parcel: Parcel) : this() {
        urlbase = parcel.readString()
        logoUrl = parcel.readString()
        impresora = parcel.readString()
        pageSize = parcel.readString()
        isLog = parcel.readInt()
        var pageSize: String

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(urlbase)
        parcel.writeString(logoUrl)
        parcel.writeString(impresora)
        parcel.writeString(pageSize)
        parcel.writeInt(isLog)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SettingsEntity> {
        override fun createFromParcel(parcel: Parcel): SettingsEntity {
            return SettingsEntity(parcel)
        }

        override fun newArray(size: Int): Array<SettingsEntity?> {
            return arrayOfNulls(size)
        }
    }

}