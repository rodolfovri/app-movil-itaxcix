package com.rodolfo.itaxcix.data.remote.websocket

import android.util.Log
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.remote.dto.websockets.DriverAvailableResponse
import com.rodolfo.itaxcix.data.remote.dto.websockets.DriverLocationUpdateResponse
import com.rodolfo.itaxcix.data.remote.dto.websockets.DriverOfflineResponse
import com.rodolfo.itaxcix.data.remote.dto.websockets.DriverUnavailableResponse
import com.rodolfo.itaxcix.data.remote.dto.websockets.InitialDriversResponse
import com.rodolfo.itaxcix.data.remote.dto.websockets.NewDriverResponse
import com.rodolfo.itaxcix.data.remote.dto.websockets.WebSocketMessage
import com.rodolfo.itaxcix.data.remote.dto.websockets.TripResponseMessage
import com.rodolfo.itaxcix.data.remote.dto.websockets.TripStatusUpdateMessage
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CitizenWebSocketService @Inject constructor(
    client: HttpClient,
    preferencesManager: PreferencesManager
) : BaseWebSocketService(client, preferencesManager) {

    private val _availableDrivers = MutableStateFlow<List<InitialDriversResponse.DriverInfo>>(emptyList())
    val availableDrivers: StateFlow<List<InitialDriversResponse.DriverInfo>> = _availableDrivers

    // Callback para notificar cuando se reciban los conductores iniciales
    var onInitialDriversReceived: ((List<InitialDriversResponse.DriverInfo>) -> Unit)? = null

    private val _tripStatusUpdates = MutableStateFlow<TripStatusUpdateMessage?>(null)
    val tripStatusUpdates: StateFlow<TripStatusUpdateMessage?> = _tripStatusUpdates

    private val _tripResponse = MutableStateFlow<TripResponseMessage?>(null)
    val tripResponse: StateFlow<TripResponseMessage?> = _tripResponse.asStateFlow()

    override fun getWebSocketPath(): String {
        return "/ws"
    }

    override fun connect() {
        super.connect()
        scope.launch {
            delay(1000)
            val user = preferencesManager.userData.value ?: return@launch
            Log.d("CitizenWebSocket", "Enviando identificación como ciudadano, userId: ${user.id}")

            sendMessage(
                WebSocketMessage(
                    type = "identify",
                    clientType = "citizen",
                    userId = user.id,
                    citizenData = WebSocketMessage.CitizenData(
                        fullName = user.fullName,
                        location = WebSocketMessage.Location(
                            lat = user.latitude?: 0.0,
                            lng = user.longitude?: 0.0
                        ),
                        image = user.profileImage ?: "",
                        rating = user.rating
                    )
                )
            )


            // Esperar un momento para asegurarnos de que la conexión esté establecida
            delay(2000)

            // Solicitar la lista inicial de conductores
            requestInitialDrivers()

            // Iniciar el temporizador para limpiar conductores inactivos
            //startDriverCleanupTimer()
        }
    }

    override fun handleIncomingMessage(messageText: String) {
        try {
            val json = Json { ignoreUnknownKeys = true }
            val baseMessage = json.decodeFromString<Map<String, JsonElement>>(messageText)
            val messageType = baseMessage["type"]?.jsonPrimitive?.content

            Log.d("CitizenWebSocket", "Received message type: $messageType")

            when (messageType) {
                "initial_drivers" -> {
                    val initialDriversResponse = json.decodeFromString<InitialDriversResponse>(messageText)
                    handleInitialDrivers(initialDriversResponse)
                    Log.d("CitizenWebSocket", "Received initial drivers: ${initialDriversResponse.drivers.size}")
                }
                "new_driver" -> {
                    val newDriverResponse = json.decodeFromString<NewDriverResponse>(messageText)
                    handleNewDriver(newDriverResponse)
                    Log.d("CitizenWebSocket", "Received new driver: ${newDriverResponse.data.fullName}")
                }
                "driver_offline" -> {
                    val driverOfflineResponse = json.decodeFromString<DriverOfflineResponse>(messageText)
                    handleDriverOffline(driverOfflineResponse)
                    Log.d("CitizenWebSocket", "Received driver offline: ${driverOfflineResponse.data.id}")
                }
                "driver_unavailable" -> {
                    val driverUnavailableResponse = json.decodeFromString<DriverUnavailableResponse>(messageText)
                    handleDriverUnavailable(driverUnavailableResponse)
                    Log.d("CitizenWebSocket", "Received driver unavailable: ${driverUnavailableResponse.data.id}")
                }
                "driver_available" -> {
                    val driverAvailableResponse = json.decodeFromString<DriverAvailableResponse>(messageText)
                    handleDriverAvailable(driverAvailableResponse)
                    Log.d("CitizenWebSocket", "Received driver available: ${driverAvailableResponse.data.id}")
                }
                "driver_location_update" -> {
                    val driverLocationUpdateResponse = json.decodeFromString<DriverLocationUpdateResponse>(messageText)
                    handleDriverLocationUpdate(driverLocationUpdateResponse)
                    Log.d("CitizenWebSocket", "Received driver location update for ID: ${driverLocationUpdateResponse.data.id}")
                }
                "trip_response" -> {
                    val tripResponse = json.decodeFromString<TripResponseMessage>(messageText)
                    handleTripResponseMessage(tripResponse)
                    Log.d("CitizenWebSocket", "Received trip response: ${tripResponse.data.tripId}, accepted: ${tripResponse.data.accepted}")
                }
                "trip_status_update" -> {
                    val tripStatusUpdate = json.decodeFromString<TripStatusUpdateMessage>(messageText)
                    handleTripStatusUpdate(tripStatusUpdate)
                    Log.d("CitizenWebSocket", "Received trip status update: ${tripStatusUpdate.data.tripId}, status: ${tripStatusUpdate.data.status}")
                }
                else -> {
                    Log.w("CitizenWebSocket", "Unknown message type: $messageType")
                }

                // otros tipos de mensaje específicos para ciudadanos...
            }
        } catch (e: Exception) {
            Log.e("CitizenWebSocket", "Error parsing message: ${e.message}")
        }
    }

    private fun handleTripStatusUpdate(statusUpdate: TripStatusUpdateMessage) {
        scope.launch {
            _tripStatusUpdates.value = statusUpdate

            when (statusUpdate.data.status) {
                "completed" -> Log.d("CitizenWebSocket", "Trip completed: ${statusUpdate.data.tripId}")
                "canceled" -> Log.d("CitizenWebSocket", "Trip cancelled: ${statusUpdate.data.tripId}")
                else -> Log.w("CitizenWebSocket", "Unknown trip status: ${statusUpdate.data.status}")
            }
        }
    }

    private fun handleTripResponseMessage(tripResponse: TripResponseMessage) {
        scope.launch {
            try {
                _tripResponse.value = tripResponse

                if (tripResponse.data.accepted) {
                    Log.d("CitizenWebSocket", "Trip accepted: ${tripResponse.data.tripId}")
                } else {
                    Log.d("CitizenWebSocket", "Trip not accepted: ${tripResponse.data.tripId}")
                }

            } catch (e: Exception) {
                Log.e("CitizenWebSocket", "Error handling trip response: ${e.message}")
            }
        }
    }

    private fun handleDriverUnavailable(response: DriverUnavailableResponse) {
        scope.launch {
            try {
                val driverId = response.data.id
                val currentDrivers = _availableDrivers.value

                // Remover el conductor de la lista de disponibles
                val updatedDrivers = currentDrivers.filter { it.id != driverId }
                _availableDrivers.value = updatedDrivers

                Log.d("CitizenWebSocket", "Conductor $driverId ya no está disponible. Conductores restantes: ${updatedDrivers.size}")
            } catch (e: Exception) {
                Log.e("CitizenWebSocket", "Error al procesar conductor no disponible: ${e.message}")
            }
        }
    }

    private fun handleDriverAvailable(response: DriverAvailableResponse) {
        scope.launch {
            try {
                val driverData = response.data

                // Convertir a DriverInfo
                val newDriverInfo = InitialDriversResponse.DriverInfo(
                    id = driverData.id,
                    fullName = driverData.fullName,
                    image = driverData.image,
                    location = InitialDriversResponse.Location(
                        lat = driverData.location.lat,
                        lng = driverData.location.lng
                    ),
                    rating = driverData.rating,
                    timestamp = driverData.timestamp
                )

                val currentDrivers = _availableDrivers.value

                // Verificar que el conductor no esté ya en la lista
                if (!currentDrivers.any { it.id == newDriverInfo.id }) {
                    _availableDrivers.value = currentDrivers + newDriverInfo
                    Log.d("CitizenWebSocket", "Conductor ${driverData.fullName} está ahora disponible. Total conductores: ${_availableDrivers.value.size}")
                } else {
                    // Si ya está en la lista, actualizar sus datos
                    val updatedDrivers = currentDrivers.map { driver ->
                        if (driver.id == newDriverInfo.id) {
                            newDriverInfo
                        } else {
                            driver
                        }
                    }
                    _availableDrivers.value = updatedDrivers
                    Log.d("CitizenWebSocket", "Datos actualizados para conductor disponible: ${driverData.fullName}")
                }
            } catch (e: Exception) {
                Log.e("CitizenWebSocket", "Error al procesar conductor disponible: ${e.message}")
            }
        }
    }

    private fun requestInitialDrivers() {
        Log.d("CitizenWebSocket", "Solicitando lista inicial de conductores...")
        sendMessage(
            WebSocketMessage(
                type = "get_initial_drivers"
            )
        )
        Log.d("CitizenWebSocket", "Solicitud de conductores iniciales enviada")
    }

    private fun handleInitialDrivers(response: InitialDriversResponse) {
        Log.d("CitizenWebSocket", "Received ${response.drivers.size} initial drivers")

        scope.launch {
            _availableDrivers.value = response.drivers
            onInitialDriversReceived?.invoke(response.drivers)
        }
    }

    // Manejar la llegada de un nuevo conductor
    private fun handleNewDriver(response: NewDriverResponse) {
        scope.launch {
            // Convertir el nuevo conductor al formato de DriverInfo
            val newDriverInfo = InitialDriversResponse.DriverInfo(
                id = response.data.id,
                fullName = response.data.fullName,
                image = response.data.image,
                location = InitialDriversResponse.Location(
                    lat = response.data.location.lat,
                    lng = response.data.location.lng
                ),
                rating = response.data.rating,
                timestamp = response.data.timestamp
            )

            // Actualizar la lista existente añadiendo el nuevo conductor
            val currentDrivers = _availableDrivers.value
            if (!currentDrivers.any { it.id == newDriverInfo.id }) {
                _availableDrivers.value = currentDrivers + newDriverInfo
            }
        }
    }

    // Manejar la desconexión de un conductor
    private fun handleDriverOffline(response: DriverOfflineResponse) {
        scope.launch {
            try {
                val driverId = response.data.id
                val currentDrivers = _availableDrivers.value
                _availableDrivers.value = currentDrivers.filter { it.id != driverId }
                Log.d("CitizenWebSocket", "Conductor eliminado: $driverId, quedan: ${_availableDrivers.value.size}")
            } catch (e: Exception) {
                Log.e("CitizenWebSocket", "Error al procesar conductor offline: ${e.message}")
            }
        }
    }

    // Método para manejar las actualizaciones de ubicación de conductores
    private fun handleDriverLocationUpdate(response: DriverLocationUpdateResponse) {
        scope.launch {
            try {
                val driverId = response.data.id
                val newLocation = response.data.location

                // Actualizar la ubicación del conductor en la lista de conductores disponibles
                val currentDrivers = _availableDrivers.value
                val updatedDrivers = currentDrivers.map { driver ->
                    if (driver.id == driverId) {
                        // Actualizar ubicación para este conductor
                        driver.copy(
                            location = InitialDriversResponse.Location(
                                lat = newLocation.lat,
                                lng = newLocation.lng
                            ),
                            // Actualizar el timestamp para evitar que se considere inactivo
                            timestamp = System.currentTimeMillis()
                        )
                    } else {
                        // Mantener los datos originales para otros conductores
                        driver
                    }
                }

                // Comprobar si se actualizó algún conductor
                val driverFound = updatedDrivers.any { it.id == driverId }

                if (driverFound) {
                    _availableDrivers.value = updatedDrivers
                    Log.d("CitizenWebSocket", "Ubicación actualizada para conductor ID: $driverId")
                } else {
                    Log.d("CitizenWebSocket", "Conductor no encontrado para actualizar ubicación: $driverId")
                    // Opcionalmente, podrías solicitar la lista de conductores nuevamente si esto ocurre con frecuencia
                }
            } catch (e: Exception) {
                Log.e("CitizenWebSocket", "Error al actualizar ubicación del conductor: ${e.message}")
            }
        }
    }

    // Iniciar un temporizador para limpiar conductores inactivos
    private fun startDriverCleanupTimer() {
        scope.launch {
            while (true) {
                delay(60000) // Cada minuto
                val currentTime = System.currentTimeMillis()
                val threshold = 5 * 60 * 1000 // 3 minutos en milisegundos

                val previousDrivers = _availableDrivers.value
                val updatedDrivers = previousDrivers.filter { driver ->
                    currentTime - driver.timestamp < threshold
                }

                if (updatedDrivers.size < previousDrivers.size) {
                    _availableDrivers.value = updatedDrivers
                    val removedCount = previousDrivers.size - updatedDrivers.size
                    Log.d("CitizenWebSocket", "Se eliminaron $removedCount conductores inactivos")
                }
            }
        }
    }

    fun resetTripResponseState() {
        scope.launch {
            _tripResponse.value = null
        }
    }

    fun resetTripStatusUpdates() {
        scope.launch {
            _tripStatusUpdates.value = null
        }
    }
}