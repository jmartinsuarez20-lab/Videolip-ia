package com.ritsuai.launcher.models

import android.graphics.drawable.Drawable

/**
 * Modelo que representa la información de un widget.
 *
 * @property id Identificador único del widget
 * @property name Nombre del widget
 * @property type Tipo de widget (weather, calendar, music, etc.)
 * @property icon Icono del widget
 * @property config Configuración específica del widget en formato JSON
 */
data class WidgetInfo(
    val id: Int,
    val name: String,
    val type: String,
    val icon: Drawable?,
    val config: String = "{}"
)

