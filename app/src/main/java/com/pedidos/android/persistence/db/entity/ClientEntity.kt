package com.pedidos.android.persistence.db.entity

import android.arch.persistence.room.Ignore
import android.os.Parcel
import android.os.Parcelable
import com.pedidos.android.persistence.model.Client
import com.google.gson.annotations.SerializedName

class ClientEntity() : Client, Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fullName)
        parcel.writeString(address)
        parcel.writeString(email)
        parcel.writeString(documentNumber)
        parcel.writeInt(identityDocumentType)
        parcel.writeString(phone)
    }

    override fun describeContents(): Int {
        return 0
    }

    @Ignore
    constructor(client: Client) : this() {

        this.identityDocumentType = client.identityDocumentType
        this.documentNumber = client.documentNumber
        this.fullName = client.fullName
        this.address = client.address
        this.email = client.email
    }

    @Ignore
    constructor(client: ClientResponseEntity) : this() {

        this.identityDocumentType = client.identityDocument
        this.documentNumber = client.documentNumber
        this.fullName = client.fullName
        this.address = client.address
        this.email = client.email
        this.phone = client.phone

    }

    @Ignore
    constructor(parcel: Parcel) : this() {
        this.identityDocumentType = parcel.readInt()
        this.documentNumber = parcel.readString()
        this.fullName  = parcel.readString()
        this.address  = parcel.readString()
        this.email  = parcel.readString()
        this.phone = parcel.readString()
    }

    @SerializedName("tipodocumento")
    override var identityDocumentType: Int = 0

    @SerializedName("numerodocumento")
    override var documentNumber: String = ""

    @SerializedName("razonsocial")
    override var fullName: String = ""

    @SerializedName("direccion")
    override var address: String = ""

    @SerializedName("correo")
    override var email: String = ""

    @SerializedName("usuario")
    var userName: String = ""

    @SerializedName("tienda")
    var storeName: String = ""

    @SerializedName("telefono")
    override var phone : String = ""
    companion object CREATOR : Parcelable.Creator<ClientEntity> {
        override fun createFromParcel(parcel: Parcel): ClientEntity {
            return ClientEntity(parcel)
        }

        override fun newArray(size: Int): Array<ClientEntity?> {
            return arrayOfNulls(size)
        }
    }
}