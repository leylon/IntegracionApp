package com.pedidos.android.persistence.utils

class ApiWrapper<T> {
    var result: Boolean = false
    var message: String = ""
    var data: T? = null
}