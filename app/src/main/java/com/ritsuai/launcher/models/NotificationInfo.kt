package com.ritsuai.launcher.models

/**
 * Modelo que representa la información de una notificación.
 *
 * @property id Identificador único de la notificación
 * @property packageName Nombre del paquete de la aplicación que generó la notificación
 * @property title Título de la notificación
 * @property content Contenido de la notificación
 * @property timestamp Timestamp de la notificación
 * @property isRead Indica si la notificación ha sido leída
 * @property priority Prioridad de la notificación
 * @property category Categoría de la notificación
 */
data class NotificationInfo(
    val id: Int,
    val packageName: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val priority: Int = 0,
    val category: String = ""
)

