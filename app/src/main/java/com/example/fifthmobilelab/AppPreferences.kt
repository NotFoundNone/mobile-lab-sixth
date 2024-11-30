package com.example.fifthmobilelab

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore instance as a singleton
private val Context.dataStore by preferencesDataStore(name = "settings")

object AppPreferences {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications")
    private val FONT_SIZE_KEY = stringPreferencesKey("font_size")
    private val LANGUAGE_KEY = stringPreferencesKey("language")

    // Theme
    fun isDarkMode(context: Context): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY] ?: false
    }

    suspend fun setDarkMode(context: Context, isDarkMode: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = isDarkMode
        }
    }

    // Notifications
    fun areNotificationsEnabled(context: Context): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_KEY] ?: true
    }

    suspend fun setNotifications(context: Context, isEnabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_KEY] = isEnabled
        }
    }

    // Font Size
    fun getFontSize(context: Context): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[FONT_SIZE_KEY] ?: "Medium"
    }

    suspend fun setFontSize(context: Context, fontSize: String) {
        context.dataStore.edit { prefs ->
            prefs[FONT_SIZE_KEY] = fontSize
        }
    }

    // Language
    fun getLanguage(context: Context): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LANGUAGE_KEY] ?: "English"
    }

    suspend fun setLanguage(context: Context, language: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language
        }
    }
}
