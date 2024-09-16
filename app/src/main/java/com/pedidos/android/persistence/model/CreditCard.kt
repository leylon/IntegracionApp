package com.pedidos.android.persistence.model

import com.google.gson.annotations.SerializedName
import com.pedidos.android.persistence.R
import java.io.Serializable

class CreditCard {
    companion object {
        const val VISA = 1
        const val MASTERCARD = 2
        const val AMERICAN_EXPRESS = 3
        const val DINERS = 4
        const val ESTILOS = 6

        fun getAll(): LinkedHashMap<Int, String> {
            val map = linkedMapOf<Int, String>()
            map[VISA] = "VISA"
            map[MASTERCARD] = "MASTERCARD"
            map[AMERICAN_EXPRESS] = "AMERICAN EXPRESS"
            map[DINERS] = "DINERS"
            map[ESTILOS] = "ESTILOS"
            return map
        }

        fun getValByPosition(position: Int): Int {
            return getAll().keys.elementAt(position)
        }

        fun getPositionByVal(value: Int): Int {
            return getAll().keys.toList().indexOf(value)
        }
    }
}

class SelectedCreditCard (
    var isSelected : Boolean = false,
    @SerializedName("codigotarjeta")
    var codeCard : Int,
    @SerializedName("descripciontarjeta")
    var description : String,
    @SerializedName("iconotarjeta")
    var icon : String
        ) : Serializable {
            fun getImageResource(icon : String) : Int {
                return when(icon) {
                    "visa" -> R.drawable.visa_pos_fc
                    "mastercard" -> R.drawable.master_card
                    "american express" -> R.drawable.american_xpress
                    "estilos"-> R.drawable.estilos
                    "dinners"-> R.drawable.dinner_card
                    "ripley" -> R.drawable.ripley_card
                    "saga falabella"-> R.drawable.saga_card
                    else -> R.drawable.other_card
                }
            }
        }

class SelectedOtherPayment(
    var isSelected : Boolean = false,
    @SerializedName("codigoformapago")
    var codeOther : Int,
    @SerializedName("descripcionformapago")
    var description : String,
    @SerializedName("iconoformapago")
    var icon : String
) : Serializable {
    fun getImageResource(icon : String) : Int {
        return when(icon) {
            "rappi" -> R.drawable.rappi_ic
            "mercadolibre" -> R.drawable.mercado_libre
            "linio" -> R.drawable.linio_ic
            "lumingo"-> R.drawable.lumingo_ic
            "orbis"-> R.drawable.orbis_ic
            else -> R.drawable.other_service
        }
    }
}

