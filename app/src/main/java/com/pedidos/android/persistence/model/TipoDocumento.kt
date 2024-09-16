package com.pedidos.android.persistence.model

class TipoDocumento {
    companion object {
        const val DNI = 1
        const val CARNET_EXTRANGERIA = 4
        const val RUC = 6
        const val PASAPORTE = 7

        fun getAll(): LinkedHashMap<Int, String> {
            val map = linkedMapOf<Int, String>()
            map[DNI] = "D.N.I."
            map[CARNET_EXTRANGERIA] = "CARNET EXTRANJERIA"
            map[RUC] = "R.U.C."
            map[PASAPORTE] = "PASAPORTE"
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