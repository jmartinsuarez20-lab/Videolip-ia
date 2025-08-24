package com.ritsuai.launcher.viewmodels

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritsuai.launcher.models.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel para la actividad principal.
 * Gestiona la lista de aplicaciones instaladas y la funcionalidad de búsqueda.
 */
class MainViewModel : ViewModel() {

    // LiveData para la lista de aplicaciones
    private val _appList = MutableLiveData<List<AppInfo>>()
    val appList: LiveData<List<AppInfo>> = _appList
    
    // Lista completa de aplicaciones (para filtrado)
    private var fullAppList: List<AppInfo> = emptyList()
    
    /**
     * Carga la lista de aplicaciones instaladas en el dispositivo
     */
    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val packageManager = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null)
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                
                val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)
                
                resolveInfoList.map { resolveInfo ->
                    val appName = resolveInfo.loadLabel(packageManager).toString()
                    val packageName = resolveInfo.activityInfo.packageName
                    val icon = resolveInfo.loadIcon(packageManager)
                    
                    AppInfo(appName, packageName, icon)
                }.sortedBy { it.appName }
            }
            
            fullAppList = apps
            _appList.value = apps
        }
    }
    
    /**
     * Filtra la lista de aplicaciones según el texto de búsqueda
     */
    fun filterApps(query: String) {
        if (query.isEmpty()) {
            _appList.value = fullAppList
            return
        }
        
        val filteredList = fullAppList.filter {
            it.appName.contains(query, ignoreCase = true) ||
            it.packageName.contains(query, ignoreCase = true)
        }
        
        _appList.value = filteredList
    }
}

