package com.pedidos.android.persistence.model.transfer

interface OnClickTransfer<T> {
    fun onClickDataListener(objectData: T)
}