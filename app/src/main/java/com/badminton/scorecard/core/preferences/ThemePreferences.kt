package com.badminton.scorecard.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_preferences")

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_KEY = stringPreferencesKey("theme_mode")
    private val PLAYER_POINT_ATTRIBUTION_KEY = booleanPreferencesKey("default_player_point_attribution")

    val defaultPlayerPointAttribution: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PLAYER_POINT_ATTRIBUTION_KEY] ?: false
    }

    suspend fun setDefaultPlayerPointAttribution(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PLAYER_POINT_ATTRIBUTION_KEY] = enabled
        }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        when (preferences[THEME_KEY]) {
            ThemeMode.LIGHT.name -> ThemeMode.LIGHT
            ThemeMode.DARK.name -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name
        }
    }

    private val GOOGLE_EMAIL_KEY = stringPreferencesKey("google_user_email")
    private val GOOGLE_NAME_KEY = stringPreferencesKey("google_user_name")
    private val GOOGLE_PHOTO_KEY = stringPreferencesKey("google_user_photo")
    private val GOOGLE_LAST_SYNC_KEY = stringPreferencesKey("google_last_sync")

    val googleUserEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[GOOGLE_EMAIL_KEY]
    }
    val googleUserName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[GOOGLE_NAME_KEY]
    }
    val googleUserPhoto: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[GOOGLE_PHOTO_KEY]
    }
    val googleLastSync: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[GOOGLE_LAST_SYNC_KEY]
    }

    suspend fun saveGoogleUser(email: String, name: String?, photoUrl: String?) {
        context.dataStore.edit { preferences ->
            preferences[GOOGLE_EMAIL_KEY] = email
            if (name != null) preferences[GOOGLE_NAME_KEY] = name else preferences.remove(GOOGLE_NAME_KEY)
            if (photoUrl != null) preferences[GOOGLE_PHOTO_KEY] = photoUrl else preferences.remove(GOOGLE_PHOTO_KEY)
        }
    }

    suspend fun clearGoogleUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(GOOGLE_EMAIL_KEY)
            preferences.remove(GOOGLE_NAME_KEY)
            preferences.remove(GOOGLE_PHOTO_KEY)
            preferences.remove(GOOGLE_LAST_SYNC_KEY)
        }
    }

    suspend fun setLastSyncTime(timeString: String) {
        context.dataStore.edit { preferences ->
            preferences[GOOGLE_LAST_SYNC_KEY] = timeString
        }
    }
}
