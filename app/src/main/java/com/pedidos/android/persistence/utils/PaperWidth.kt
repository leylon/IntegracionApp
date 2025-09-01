package com.pedidos.android.persistence.utils

enum class PaperWidth(val widthValue: String) {
    WIDTH_58MM("58mm"),
    WIDTH_80MM("80mm"),
    WIDTH_50_8MM("50.8mm"),
    WIDTH_48MM("48mm")
}

fun detectPaperSize(): PaperWidth {
    // Aquí implementarías la lógica para detectar el tamaño
    // Podría ser mediante selección manual, configuración o sensor si la impresora lo soporta
    return PaperWidth.WIDTH_80MM // Ejemplo por defecto
}