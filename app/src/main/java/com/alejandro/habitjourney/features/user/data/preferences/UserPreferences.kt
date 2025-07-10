package com.alejandro.habitjourney.features.user.data.preferences

import android.content.Context
import android.content.SharedPreferences
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


/**
 * Extensión de [Context] para crear una instancia de [DataStore] para las preferencias de usuario.
 * Esta es la forma recomendada de inicializar DataStore.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Clase singleton para gestionar las preferencias de usuario, como tokens de autenticación e ID de usuario.
 *
 * Utiliza [DataStore Preferences] como fuente principal de verdad para la persistencia de datos,
 * ofreciendo un [Flow] reactivo para observar cambios. También incluye un respaldo en
 * [SharedPreferences] para casos específicos que requieran acceso síncrono, como interceptores de red.
 *
 * @property context El contexto de la aplicación, inyectado por Dagger Hilt.
 */
@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    private companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val USER_ID = stringPreferencesKey("user_id")
    }

    /**
     * Un [Flow] reactivo que emite el token de autenticación actual almacenado.
     * Emitirá `null` si no hay ningún token guardado.
     */
    val authTokenFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[AUTH_TOKEN]
        }

    /**
     * Obtiene el token de autenticación de forma asíncrona.
     * Este es un metodo suspendido y bloqueará hasta que se emita el primer valor del [authTokenFlow].
     *
     * @return El token de autenticación como [String] o `null` si no está presente.
     */
    suspend fun getAuthToken(): String? {
        return authTokenFlow.first()
    }

    /**
     * Obtiene el token de autenticación de forma síncrona.
     *
     * **Advertencia:** Este metodo utiliza [SharedPreferences] como un respaldo para casos
     * donde se necesita acceso inmediato y no se puede usar un [Flow] (ej. en interceptores de red
     * donde el contexto suspendido no está disponible). La fuente de verdad principal es [DataStore].
     *
     * @return El token de autenticación como [String] o `null` si no está presente.
     */
    fun getAuthTokenSync(): String? {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }

    /**
     * Guarda el token de autenticación tanto en [DataStore] como en [SharedPreferences] (como respaldo).
     *
     * @param token El token de autenticación a guardar.
     */
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }

        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("auth_token", token)
            .apply()
    }

    /**
     * Guarda el ID del usuario en [DataStore].
     *
     * @param userId El ID del usuario a guardar.
     */
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId.toString()
        }
    }

    /**
     * Un [Flow] reactivo que emite el ID del usuario actual almacenado.
     * Emitirá `null` si no hay ningún ID de usuario guardado o si la conversión falla.
     */
    val userIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID]
        }

    /**
     * Obtiene el ID del usuario de forma asíncrona.
     * Bloqueará hasta que se emita el primer valor del [userIdFlow].
     *
     * @return El ID del usuario como [String] o `null` si no está presente.
     */
    suspend fun getUserId(): String? {
        return userIdFlow.first()
    }

    /**
     * Obtiene un [Flow] que emite el ID del usuario actual.
     * Este metodo proporciona un acceso reactivo al ID del usuario.
     *
     * @return Un [Flow] que emite el ID del usuario como [String] o `null`.
     */
    fun getCurrentUserId(): Flow<String?> {
        return userIdFlow
    }

    /**
     * Limpia todos los datos almacenados tanto en [DataStore] como en [SharedPreferences].
     * Esto se usa típicamente para cerrar la sesión del usuario.
     */
    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }

        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}