package com.pedidos.android.persistence.utils

import com.pedidos.android.persistence.db.entity.ClientEntity
import com.pedidos.android.persistence.model.TipoDocumento

class Defaults {
    companion object {
        val Cliente = ClientEntity().apply {
            documentNumber = "99999999999"
            fullName = "CLIENTE GENERICO"
            identityDocumentType = TipoDocumento.DNI
        }

        val FACTURA = "FAC"
        val BOLETA = "BOL"
    }
}