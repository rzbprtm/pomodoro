package com.pomodoro.focus.ui.settings

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pomodoro.focus.data.preferences.SettingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppInfo(
    val packageName: String,
    val label: String,
    val isWhitelisted: Boolean
)

class SettingsViewModel(
    application: Application,
    private val settingsDataStore: SettingsDataStore
) : AndroidViewModel(application) {

    val isDarkMode = settingsDataStore.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val focusMinutes = settingsDataStore.focusMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50)

    val restMinutes = settingsDataStore.restMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    val autoDnd = settingsDataStore.autoDnd
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autostartConfirmed = settingsDataStore.autostartConfirmed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val whitelistPackages = settingsDataStore.whitelistPackages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), setOf("com.whatsapp"))

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val installedApps: StateFlow<List<AppInfo>> = combine(whitelistPackages, _searchQuery) { whitelist, query ->
        val pm = application.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // user apps only
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    label = pm.getApplicationLabel(appInfo).toString(),
                    isWhitelisted = whitelist.contains(appInfo.packageName)
                )
            }
            .sortedWith(compareByDescending<AppInfo> { it.isWhitelisted }.thenBy { it.label })

        if (query.isBlank()) apps
        else apps.filter {
            it.label.contains(query, ignoreCase = true) ||
            it.packageName.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setDarkMode(enabled) }
    }

    fun setFocusMinutes(m: Int) {
        viewModelScope.launch { settingsDataStore.setFocusMinutes(m) }
    }

    fun setRestMinutes(m: Int) {
        viewModelScope.launch { settingsDataStore.setRestMinutes(m) }
    }

    fun toggleAutoDnd(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setAutoDnd(enabled) }
    }

    fun setAutostartConfirmed(confirmed: Boolean) {
        viewModelScope.launch { settingsDataStore.setAutostartConfirmed(confirmed) }
    }

    fun toggleWhitelist(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            val current = whitelistPackages.value.toMutableSet()
            if (enabled) current.add(packageName) else current.remove(packageName)
            settingsDataStore.setWhitelistPackages(current)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
