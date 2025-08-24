package com.ritsuai.launcher.models

import android.graphics.drawable.Drawable

/**
 * Modelo de datos para información de aplicaciones instaladas.
 *
 * @property appName Nombre de la aplicación
 * @property packageName Nombre del paquete de la aplicación
 * @property icon Icono de la aplicación
 */
data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable
)

