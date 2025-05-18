package com.rodolfo.itaxcix.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PreferencesManager es una clase que maneja el almacenamiento de preferencias de usuario utilizando
 * DataStore. Permite guardar, cargar y limpiar datos de usuario.
 *
 * @param dataStore Instancia de DataStore<Preferences> para acceder a las preferencias.
 */

class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    // Definir claves para preferencias
    private object PreferencesKeys {
        val USER_ID = intPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_NICKNAME = stringPreferencesKey("user_nickname")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val USER_ADDRESS = stringPreferencesKey("user_address")
        val USER_CITY = stringPreferencesKey("user_city")
        val USER_COUNTRY = stringPreferencesKey("user_country")
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_STATUS = stringPreferencesKey("user_status")
        val IS_DRIVER_AVAILABLE = booleanPreferencesKey("is_driver_available")
        val LAST_DRIVER_UPDATE = stringPreferencesKey("last_driver_update")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    // StateFlow observable para datos de usuario
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    // Cargar datos almacenados
    init {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.data.collect { preferences ->
                val userId = preferences[PreferencesKeys.USER_ID]
                val userName = preferences[PreferencesKeys.USER_NAME]
                val nickname = preferences[PreferencesKeys.USER_NICKNAME]
                val email = preferences[PreferencesKeys.USER_EMAIL] ?: ""
                val phone = preferences[PreferencesKeys.USER_PHONE] ?: ""
                val address = preferences[PreferencesKeys.USER_ADDRESS] ?: ""
                val city = preferences[PreferencesKeys.USER_CITY] ?: ""
                val country = preferences[PreferencesKeys.USER_COUNTRY] ?: ""
                val userRole = preferences[PreferencesKeys.USER_ROLE]
                val userStatus = preferences[PreferencesKeys.USER_STATUS]
                val isDriverAvailable = preferences[PreferencesKeys.IS_DRIVER_AVAILABLE]
                val lastDriverUpdate = preferences[PreferencesKeys.LAST_DRIVER_UPDATE]
                val authToken = preferences[PreferencesKeys.AUTH_TOKEN]

                if (userId != null && userName != null && nickname != null) {
                    _userData.value = UserData(
                        id = userId,
                        name = userName,
                        nickname = nickname,
                        email = email,
                        phone = phone,
                        address = address,
                        city = city,
                        country = country,
                        role = userRole,
                        status = userStatus,
                        isDriverAvailable = isDriverAvailable,
                        lastDriverStatusUpdate = lastDriverUpdate,
                        authToken = authToken
                    )
                }
            }
        }
    }

    // Guardar datos de usuario
    suspend fun saveUserData(userData: UserData) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userData.id
            preferences[PreferencesKeys.USER_NAME] = userData.name
            preferences[PreferencesKeys.USER_NICKNAME] = userData.nickname
            preferences[PreferencesKeys.USER_EMAIL] = userData.email
            preferences[PreferencesKeys.USER_PHONE] = userData.phone
            preferences[PreferencesKeys.USER_ADDRESS] = userData.address
            preferences[PreferencesKeys.USER_CITY] = userData.city
            preferences[PreferencesKeys.USER_COUNTRY] = userData.country
            preferences[PreferencesKeys.USER_ROLE] = userData.role ?: ""
            preferences[PreferencesKeys.USER_STATUS] = userData.status ?: ""

            userData.isDriverAvailable?.let {
                preferences[PreferencesKeys.IS_DRIVER_AVAILABLE] = it
            }

            userData.lastDriverStatusUpdate?.let {
                preferences[PreferencesKeys.LAST_DRIVER_UPDATE] = it
            }

            userData.authToken?.let {
                preferences[PreferencesKeys.AUTH_TOKEN] = it
            }
        }
        _userData.value = userData
    }

    // Limpiar datos (logout)
    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
        _userData.value = null
    }
}

data class UserData(
    val id: Int,
    val name: String,
    val nickname: String,
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val city: String = "",
    val country: String = "",
    val role: String? = null,
    val status: String? = null,
    val isDriverAvailable: Boolean? = null,
    val lastDriverStatusUpdate: String? = null,
    val authToken: String? = null
)