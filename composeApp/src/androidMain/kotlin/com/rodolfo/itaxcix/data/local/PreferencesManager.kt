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
        val USER_FIRST_NAME = stringPreferencesKey("user_first_name")
        val USER_LAST_NAME = stringPreferencesKey("user_last_name")
        val USER_FULL_NAME = stringPreferencesKey("user_full_name")
        val USER_IS_TUC_ACTIVE = booleanPreferencesKey("user_is_tuc_active")
        val USER_RATING = stringPreferencesKey("user_rating")
        val USER_NICKNAME = stringPreferencesKey("user_nickname")
        val USER_DOCUMENT = stringPreferencesKey("user_document")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val USER_ADDRESS = stringPreferencesKey("user_address")
        val USER_CITY = stringPreferencesKey("user_city")
        val USER_COUNTRY = stringPreferencesKey("user_country")
        val USER_ROLES = stringPreferencesKey("user_roles") // Cambiado de USER_ROLE a USER_ROLES
        val USER_PERMISSIONS = stringPreferencesKey("user_permissions") // Nueva clave
        val USER_STATUS = stringPreferencesKey("user_status")
        val IS_DRIVER_AVAILABLE = booleanPreferencesKey("is_driver_available")
        val LAST_DRIVER_UPDATE = stringPreferencesKey("last_driver_update")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_PROFILE_IMAGE = stringPreferencesKey("user_profile_image")
        val USER_LATITUDE = stringPreferencesKey("user_latitude")
        val USER_LONGITUDE = stringPreferencesKey("user_longitude")
    }

    // StateFlow observable para datos de usuario
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    // Cargar datos almacenados
    init {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.data.collect { preferences ->
                val userId = preferences[PreferencesKeys.USER_ID]
                val userFirstName = preferences[PreferencesKeys.USER_FIRST_NAME]
                val userLastName = preferences[PreferencesKeys.USER_LAST_NAME]
                val userFullName = preferences[PreferencesKeys.USER_FULL_NAME] ?: ""
                val userIsTucActive = preferences[PreferencesKeys.USER_IS_TUC_ACTIVE]
                val userRating = preferences[PreferencesKeys.USER_RATING]?.toDoubleOrNull() ?: 0.0
                val nickname = preferences[PreferencesKeys.USER_NICKNAME]
                val userDocument = preferences[PreferencesKeys.USER_DOCUMENT] ?: ""
                val email = preferences[PreferencesKeys.USER_EMAIL] ?: ""
                val phone = preferences[PreferencesKeys.USER_PHONE] ?: ""
                val address = preferences[PreferencesKeys.USER_ADDRESS] ?: ""
                val city = preferences[PreferencesKeys.USER_CITY] ?: ""
                val country = preferences[PreferencesKeys.USER_COUNTRY] ?: ""
                val userRole = preferences[PreferencesKeys.USER_ROLES]
                val userPermissions = preferences[PreferencesKeys.USER_PERMISSIONS] ?: ""
                val userStatus = preferences[PreferencesKeys.USER_STATUS]
                val isDriverAvailable = preferences[PreferencesKeys.IS_DRIVER_AVAILABLE]
                val lastDriverUpdate = preferences[PreferencesKeys.LAST_DRIVER_UPDATE]
                val authToken = preferences[PreferencesKeys.AUTH_TOKEN]
                val profileImage = preferences[PreferencesKeys.USER_PROFILE_IMAGE]
                val latitude = preferences[PreferencesKeys.USER_LATITUDE]?.toDoubleOrNull()
                val longitude = preferences[PreferencesKeys.USER_LONGITUDE]?.toDoubleOrNull()

                if (userId != null && userFirstName != null && userLastName != null) {
                    _userData.value = UserData(
                        id = userId,
                        firstName = userFirstName,
                        lastName = userLastName,
                        fullName = userFullName,
                        isTucActive = userIsTucActive,
                        rating = userRating,
                        nickname = nickname ?: "",
                        document = userDocument,
                        email = email,
                        phone = phone,
                        address = address,
                        city = city,
                        country = country,
                        roles = userRole?.split(",") ?: emptyList(),
                        permissions = userPermissions.split(",").filter { it.isNotEmpty() },
                        status = userStatus,
                        isDriverAvailable = isDriverAvailable,
                        lastDriverStatusUpdate = lastDriverUpdate,
                        authToken = authToken,
                        profileImage = profileImage,
                        latitude = latitude,
                        longitude = longitude

                    )
                }
            }
        }
    }

    // Guardar datos de usuario
    suspend fun saveUserData(userData: UserData) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userData.id
            preferences[PreferencesKeys.USER_FIRST_NAME] = userData.firstName
            preferences[PreferencesKeys.USER_LAST_NAME] = userData.lastName
            preferences[PreferencesKeys.USER_FULL_NAME] = userData.fullName
            preferences[PreferencesKeys.USER_NICKNAME] = userData.nickname
            preferences[PreferencesKeys.USER_EMAIL] = userData.email
            preferences[PreferencesKeys.USER_PHONE] = userData.phone
            preferences[PreferencesKeys.USER_ADDRESS] = userData.address
            preferences[PreferencesKeys.USER_CITY] = userData.city
            preferences[PreferencesKeys.USER_COUNTRY] = userData.country
            preferences[PreferencesKeys.USER_ROLES] = userData.roles.joinToString(",")
            preferences[PreferencesKeys.USER_PERMISSIONS] = userData.permissions.joinToString(",")
            preferences[PreferencesKeys.USER_STATUS] = userData.status ?: ""

            userData.isTucActive?.let {
                preferences[PreferencesKeys.USER_IS_TUC_ACTIVE] = it
            }

            userData.isDriverAvailable?.let {
                preferences[PreferencesKeys.IS_DRIVER_AVAILABLE] = it
            }

            userData.lastDriverStatusUpdate?.let {
                preferences[PreferencesKeys.LAST_DRIVER_UPDATE] = it
            }

            userData.authToken?.let {
                preferences[PreferencesKeys.AUTH_TOKEN] = it
            }

            userData.profileImage?.let {
                preferences[PreferencesKeys.USER_PROFILE_IMAGE] = it
            }

            userData.latitude?.let {
                preferences[PreferencesKeys.USER_LATITUDE] = it.toString()
            }

            userData.longitude?.let {
                preferences[PreferencesKeys.USER_LONGITUDE] = it.toString()
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
    val firstName: String,
    val lastName: String,
    val fullName: String = "$firstName $lastName",
    val isTucActive: Boolean? = null,
    val rating: Double,
    val nickname: String,
    val document: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val city: String = "",
    val country: String = "",
    val roles: List<String> = emptyList(),
    val permissions: List<String> = emptyList(),
    val status: String? = null,
    val isDriverAvailable: Boolean? = null,
    val lastDriverStatusUpdate: String? = null,
    val authToken: String? = null,
    val profileImage: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)