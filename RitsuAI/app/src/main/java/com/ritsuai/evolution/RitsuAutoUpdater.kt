package com.ritsuai.evolution

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Sistema de actualización automática para Ritsu que permite
 * descargar e instalar actualizaciones desde GitHub.
 */
class RitsuAutoUpdater(private val context: Context) {
    
    private val TAG = "RitsuAutoUpdater"
    private val isChecking = AtomicBoolean(false)
    private var downloadId: Long = -1
    
    // URL base del repositorio de GitHub
    private val GITHUB_API_URL = "https://api.github.com/repos/jmartinsuarez20-lab/Videolip-ia/releases/latest"
    
    // Receptor para manejar la finalización de la descarga
    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                Log.d(TAG, "Descarga completada, iniciando instalación")
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val uri = downloadManager.getUriForDownloadedFile(downloadId)
                if (uri != null) {
                    installUpdate(uri)
                }
            }
        }
    }
    
    init {
        // Registrar el receptor para la finalización de descargas
        context.registerReceiver(
            downloadCompleteReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }
    
    /**
     * Verifica si hay actualizaciones disponibles
     */
    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        if (isChecking.getAndSet(true)) {
            Log.d(TAG, "Ya hay una verificación de actualizaciones en curso")
            return@withContext null
        }
        
        try {
            // Obtener información de la versión actual
            val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            Log.d(TAG, "Versión actual: $currentVersion")
            
            // Obtener información de la última versión desde GitHub
            val latestRelease = getLatestReleaseInfo()
            if (latestRelease != null) {
                val latestVersion = latestRelease.version
                Log.d(TAG, "Última versión disponible: $latestVersion")
                
                // Comparar versiones
                if (isNewerVersion(currentVersion, latestVersion)) {
                    return@withContext latestRelease
                } else {
                    Log.d(TAG, "La aplicación ya está actualizada")
                }
            }
            
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar actualizaciones: ${e.message}")
            return@withContext null
        } finally {
            isChecking.set(false)
        }
    }
    
    /**
     * Descarga la actualización
     */
    fun downloadUpdate(updateInfo: UpdateInfo): Boolean {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            
            val request = DownloadManager.Request(Uri.parse(updateInfo.downloadUrl))
                .setTitle("Actualización de Ritsu")
                .setDescription("Descargando versión ${updateInfo.version}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "ritsu_update.apk")
            
            downloadId = downloadManager.enqueue(request)
            Log.d(TAG, "Descarga iniciada con ID: $downloadId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar la descarga: ${e.message}")
            return false
        }
    }
    
    /**
     * Instala la actualización descargada
     */
    private fun installUpdate(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // Para Android 7.0 y superior, usar FileProvider
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        File(uri.path ?: "")
                    )
                    setDataAndType(contentUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    // Para versiones anteriores
                    setDataAndType(uri, "application/vnd.android.package-archive")
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            Log.d(TAG, "Instalación iniciada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar la instalación: ${e.message}")
        }
    }
    
    /**
     * Obtiene información de la última versión desde GitHub
     */
    private fun getLatestReleaseInfo(): UpdateInfo? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(GITHUB_API_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                
                // Parsear la respuesta JSON
                val jsonResponse = JSONObject(response.toString())
                val tagName = jsonResponse.getString("tag_name")
                val releaseNotes = jsonResponse.getString("body")
                
                // Obtener URL de descarga del APK
                val assets = jsonResponse.getJSONArray("assets")
                var downloadUrl = ""
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.getString("name")
                    if (name.endsWith(".apk")) {
                        downloadUrl = asset.getString("browser_download_url")
                        break
                    }
                }
                
                if (downloadUrl.isNotEmpty()) {
                    return UpdateInfo(tagName, releaseNotes, downloadUrl)
                }
            } else {
                Log.e(TAG, "Error al obtener información de GitHub: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al conectar con GitHub: ${e.message}")
        } finally {
            connection?.disconnect()
        }
        
        return null
    }
    
    /**
     * Compara versiones para determinar si hay una nueva versión disponible
     */
    private fun isNewerVersion(currentVersion: String, latestVersion: String): Boolean {
        // Eliminar el prefijo 'v' si existe
        val current = currentVersion.removePrefix("v")
        val latest = latestVersion.removePrefix("v")
        
        // Dividir las versiones en componentes
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        
        // Comparar cada componente
        for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
            val currentPart = currentParts.getOrNull(i) ?: 0
            val latestPart = latestParts.getOrNull(i) ?: 0
            
            if (latestPart > currentPart) return true
            if (latestPart < currentPart) return false
        }
        
        return false // Versiones iguales
    }
    
    /**
     * Libera recursos al destruir
     */
    fun destroy() {
        try {
            context.unregisterReceiver(downloadCompleteReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error al desregistrar receptor: ${e.message}")
        }
    }
    
    /**
     * Clase de datos para información de actualizaciones
     */
    data class UpdateInfo(
        val version: String,
        val releaseNotes: String,
        val downloadUrl: String
    )
}

