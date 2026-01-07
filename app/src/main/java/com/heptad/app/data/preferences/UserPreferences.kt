package com.heptad.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "heptad_preferences")

/**
 * Theme mode options
 */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

/**
 * User preferences data class
 */
data class UserPreferencesData(
    val dictionaryLevel: Int = 70,
    val includeS: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

/**
 * Repository for managing user preferences using DataStore
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val DICTIONARY_LEVEL = intPreferencesKey("dictionary_level")
        val INCLUDE_S = booleanPreferencesKey("include_s")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    /**
     * Flow of user preferences
     */
    val preferencesFlow: Flow<UserPreferencesData> = context.dataStore.data.map { preferences ->
        UserPreferencesData(
            dictionaryLevel = preferences[PreferencesKeys.DICTIONARY_LEVEL] ?: 70,
            includeS = preferences[PreferencesKeys.INCLUDE_S] ?: true,
            themeMode = preferences[PreferencesKeys.THEME_MODE]?.let {
                ThemeMode.valueOf(it)
            } ?: ThemeMode.SYSTEM
        )
    }

    /**
     * Update dictionary level (60, 70, or 80)
     */
    suspend fun setDictionaryLevel(level: Int) {
        require(level in listOf(60, 70, 80)) { "Dictionary level must be 60, 70, or 80" }
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DICTIONARY_LEVEL] = level
        }
    }

    /**
     * Update include S preference
     */
    suspend fun setIncludeS(include: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_S] = include
        }
    }

    /**
     * Update theme mode
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }
}
