package com.ritsuai.launcher.models

import android.graphics.drawable.Drawable

/**
 * Modelo que representa la información de una aplicación instalada.
 *
 * @property appName Nombre de la aplicación
 * @property packageName Nombre del paquete de la aplicación
 * @property icon Icono de la aplicación
 * @property isFavorite Indica si la aplicación es favorita
 * @property lastUsed Timestamp de la última vez que se usó la aplicación
 * @property usageCount Contador de uso de la aplicación
 */
data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable?,
    val isFavorite: Boolean = false,
    val lastUsed: Long = 0,
    val usageCount: Int = 0
)

