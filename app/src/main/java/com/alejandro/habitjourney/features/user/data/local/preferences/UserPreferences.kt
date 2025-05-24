package com.alejandro.habitjourney.features.user.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val USER_ID = stringPreferencesKey("user_id")
    }

    // Flow reactivo para observar cambios en el token
    val authTokenFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[AUTH_TOKEN]
        }

    // Método suspend para obtener el token de forma síncrona cuando sea necesario
    suspend fun getAuthToken(): String? {
        return authTokenFlow.first()
    }

    // Método no-suspend para casos donde necesites acceso inmediato (interceptors, etc.)
    fun getAuthTokenSync(): String? {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }

    suspend fun saveAuthToken(token: String) {
        // Guardar en DataStore (fuente de verdad)
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }

        // Backup en SharedPreferences para acceso síncrono en interceptors
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("auth_token", token)
            .apply()
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }

    val userIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID]
        }

    suspend fun clear() {
        // Limpiar DataStore
        context.dataStore.edit { preferences ->
            preferences.clear()
        }

        // Limpiar SharedPreferences
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}