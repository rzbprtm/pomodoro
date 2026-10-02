package com.pomodoro.focus.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FOCUS_MINUTES = intPreferencesKey("focus_minutes")
        val REST_MINUTES = intPreferencesKey("rest_minutes")
        val WHITELIST_PACKAGES = stringSetPreferencesKey("whitelist_packages")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: true }
    val focusMinutes: Flow<Int> = context.dataStore.data.map { it[FOCUS_MINUTES] ?: 25 }
    val restMinutes: Flow<Int> = context.dataStore.data.map { it[REST_MINUTES] ?: 5 }
    val whitelistPackages: Flow<Set<String>> = context.dataStore.data.map {
        it[WHITELIST_PACKAGES] ?: setOf("com.whatsapp")
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setFocusMinutes(minutes: Int) {
        context.dataStore.edit { it[FOCUS_MINUTES] = minutes }
    }

    suspend fun setRestMinutes(minutes: Int) {
        context.dataStore.edit { it[REST_MINUTES] = minutes }
    }

    suspend fun setWhitelistPackages(packages: Set<String>) {
        context.dataStore.edit { it[WHITELIST_PACKAGES] = packages }
    }
}
