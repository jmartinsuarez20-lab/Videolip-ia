package com.ritsuai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.database.Cursor
import kotlinx.coroutines.*

/**
 * Gestor de control completo del teléfono
 * Maneja llamadas, mensajes, aplicaciones y más
 */
class PhoneControlManager(private val context: Context) {
    
    companion object {
        fun parseCommand(input: String): PhoneAction {
            val lowerInput = input.lowercase()
            
            return when {
                lowerInput.contains("llamar") -> parseCallCommand(input)
                lowerInput.contains("whatsapp") || lowerInput.contains("mensaje") -> parseMessageCommand(input)
                lowerInput.contains("abrir") -> parseAppCommand(input)
                lowerInput.contains("buscar") -> parseSearchCommand(input)
                lowerInput.contains("configurar") -> parseSettingsCommand(input)
                else -> PhoneAction(PhoneActionType.UNKNOWN, "", "")
            }
        }
        
        private fun parseCallCommand(input: String): PhoneAction {
            // Extraer número o nombre del contacto
            val target = extractContactInfo(input)
            return PhoneAction(PhoneActionType.CALL, target, "")
        }
        
        private fun parseMessageCommand(input: String): PhoneAction {
            val target = extractContactInfo(input)
            val message = extractMessageContent(input)
            return PhoneAction(PhoneActionType.MESSAGE, target, message)
        }
        
        private fun parseAppCommand(input: String): PhoneAction {
            val appName = extractAppName(input)
            return PhoneAction(PhoneActionType.OPEN_APP, appName, "")
        }
        
        private fun parseSearchCommand(input: String): PhoneAction {
            val query = extractSearchQuery(input)
            return PhoneAction(PhoneActionType.SEARCH, query, "")
        }
        
        private fun parseSettingsCommand(input: String): PhoneAction {
            val setting = extractSettingType(input)
            return PhoneAction(PhoneActionType.SETTINGS, setting, "")
        }
        
        private fun extractContactInfo(input: String): String {
            // Lógica para extraer información de contacto
            val words = input.split(" ")
            val callIndex = words.indexOfFirst { it.contains("llamar") || it.contains("mensaje") }
            
            return if (callIndex != -1 && callIndex + 1 < words.size) {
                words.subList(callIndex + 1, words.size).joinToString(" ")
                    .replace("a ", "")
                    .replace("al ", "")
                    .trim()
            } else {
                ""
            }
        }
        
        private fun extractMessageContent(input: String): String {
            val patterns = listOf("que diga", "diciendo", "mensaje:", "texto:")
            
            for (pattern in patterns) {
                val index = input.lowercase().indexOf(pattern)
                if (index != -1) {
                    return input.substring(index + pattern.length).trim()
                        .removeSurrounding("\"")
                        .removeSurrounding("'")
                }
            }
            
            return "Mensaje enviado por Ritsu"
        }
        
        private fun extractAppName(input: String): String {
            val commonApps = mapOf(
                "whatsapp" to "com.whatsapp",
                "instagram" to "com.instagram.android",
                "facebook" to "com.facebook.katana",
                "youtube" to "com.google.android.youtube",
                "spotify" to "com.spotify.music",
                "netflix" to "com.netflix.mediaclient",
                "gmail" to "com.google.android.gm",
                "chrome" to "com.android.chrome",
                "cámara" to "com.android.camera2",
                "galería" to "com.google.android.apps.photos",
                "configuración" to "com.android.settings",
                "calculadora" to "com.android.calculator2"
            )
            
            val lowerInput = input.lowercase()
            for ((name, packageName) in commonApps) {
                if (lowerInput.contains(name)) {
                    return packageName
                }
            }
            
            return ""
        }
        
        private fun extractSearchQuery(input: String): String {
            val searchIndex = input.lowercase().indexOf("buscar")
            return if (searchIndex != -1) {
                input.substring(searchIndex + 6).trim()
            } else {
                ""
            }
        }
        
        private fun extractSettingType(input: String): String {
            val lowerInput = input.lowercase()
            return when {
                lowerInput.contains("wifi") -> "wifi"
                lowerInput.contains("bluetooth") -> "bluetooth"
                lowerInput.contains("volumen") -> "volume"
                lowerInput.contains("brillo") -> "brightness"
                lowerInput.contains("modo avión") -> "airplane_mode"
                else -> "general"
            }
        }
    }
    
    /**
     * Ejecuta una acción de teléfono
     */
    suspend fun executeAction(action: PhoneAction): ActionResult {
        return withContext(Dispatchers.IO) {
            try {
                when (action.type) {
                    PhoneActionType.CALL -> makeCall(action.target)
                    PhoneActionType.MESSAGE -> sendMessage(action.target, action.content)
                    PhoneActionType.WHATSAPP -> sendWhatsAppMessage(action.target, action.content)
                    PhoneActionType.OPEN_APP -> openApp(action.target)
                    PhoneActionType.SEARCH -> performSearch(action.target)
                    PhoneActionType.SETTINGS -> changeSettings(action.target)
                    else -> ActionResult(false, "Acción no reconocida")
                }
            } catch (e: Exception) {
                ActionResult(false, "Error: ${e.message}")
            }
        }
    }
    
    private fun makeCall(target: String): ActionResult {
        return try {
            val phoneNumber = if (target.matches(Regex("\\d+"))) {
                target
            } else {
                getContactNumber(target)
            }
            
            if (phoneNumber.isNotEmpty()) {
                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(callIntent)
                ActionResult(true, "Llamando a $target...")
            } else {
                ActionResult(false, "No se encontró el contacto: $target")
            }
        } catch (e: Exception) {
            ActionResult(false, "Error al realizar la llamada: ${e.message}")
        }
    }
    
    private fun sendMessage(target: String, message: String): ActionResult {
        return try {
            val phoneNumber = if (target.matches(Regex("\\d+"))) {
                target
            } else {
                getContactNumber(target)
            }
            
            if (phoneNumber.isNotEmpty()) {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                ActionResult(true, "Mensaje enviado a $target: '$message'")
            } else {
                ActionResult(false, "No se encontró el contacto: $target")
            }
        } catch (e: Exception) {
            ActionResult(false, "Error al enviar mensaje: ${e.message}")
        }
    }
    
    private fun sendWhatsAppMessage(target: String, message: String): ActionResult {
        return try {
            val phoneNumber = if (target.matches(Regex("\\d+"))) {
                target
            } else {
                getContactNumber(target)
            }
            
            if (phoneNumber.isNotEmpty()) {
                val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(whatsappIntent)
                ActionResult(true, "Abriendo WhatsApp para enviar mensaje a $target")
            } else {
                ActionResult(false, "No se encontró el contacto: $target")
            }
        } catch (e: Exception) {
            ActionResult(false, "Error al abrir WhatsApp: ${e.message}")
        }
    }
    
    private fun openApp(packageName: String): ActionResult {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                ActionResult(true, "Abriendo aplicación...")
            } else {
                ActionResult(false, "Aplicación no encontrada")
            }
        } catch (e: Exception) {
            ActionResult(false, "Error al abrir aplicación: ${e.message}")
        }
    }
    
    private fun performSearch(query: String): ActionResult {
        return try {
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra("query", query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(searchIntent)
            ActionResult(true, "Buscando: $query")
        } catch (e: Exception) {
            ActionResult(false, "Error en la búsqueda: ${e.message}")
        }
    }
    
    private fun changeSettings(settingType: String): ActionResult {
        return try {
            val settingsIntent = when (settingType) {
                "wifi" -> Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                "bluetooth" -> Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                "volume" -> Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)
                "brightness" -> Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS)
                else -> Intent(android.provider.Settings.ACTION_SETTINGS)
            }.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(settingsIntent)
            ActionResult(true, "Abriendo configuración de $settingType")
        } catch (e: Exception) {
            ActionResult(false, "Error al abrir configuración: ${e.message}")
        }
    }
    
    private fun getContactNumber(contactName: String): String {
        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$contactName%"),
            null
        )
        
        cursor?.use {
            if (it.moveToFirst()) {
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (numberIndex >= 0) {
                    return it.getString(numberIndex)
                }
            }
        }
        
        return ""
    }
    
    /**
     * Obtiene la lista de contactos
     */
    fun getContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        
        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            while (it.moveToNext()) {
                if (nameIndex >= 0 && numberIndex >= 0) {
                    val name = it.getString(nameIndex)
                    val number = it.getString(numberIndex)
                    contacts.add(Contact(name, number))
                }
            }
        }
        
        return contacts
    }
    
    /**
     * Obtiene aplicaciones instaladas
     */
    fun getInstalledApps(): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledApplications(0)
        
        for (packageInfo in packages) {
            val appName = packageManager.getApplicationLabel(packageInfo).toString()
            val packageName = packageInfo.packageName
            
            // Filtrar solo apps que se pueden lanzar
            if (packageManager.getLaunchIntentForPackage(packageName) != null) {
                apps.add(AppInfo(appName, packageName))
            }
        }
        
        return apps.sortedBy { it.name }
    }
}

// Clases de datos para el control del teléfono
data class PhoneAction(
    val type: PhoneActionType,
    val target: String,
    val content: String
)

enum class PhoneActionType {
    CALL, MESSAGE, WHATSAPP, OPEN_APP, SEARCH, SETTINGS, UNKNOWN
}

data class ActionResult(
    val success: Boolean,
    val message: String
)

data class Contact(
    val name: String,
    val phoneNumber: String
)

data class AppInfo(
    val name: String,
    val packageName: String
)

