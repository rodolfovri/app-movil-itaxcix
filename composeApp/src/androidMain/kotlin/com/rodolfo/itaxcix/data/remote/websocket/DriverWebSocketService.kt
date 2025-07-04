package com.rodolfo.itaxcix.data.remote.websocket

import android.util.Log
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.remote.dto.websockets.UpdateLocationRequest
import com.rodolfo.itaxcix.data.remote.dto.websockets.WebSocketMessage
import com.rodolfo.itaxcix.data.remote.dto.websockets.TripRequestMessage
import com.rodolfo.itaxcix.data.remote.dto.websockets.TripStatusUpdateMessage
import io.ktor.client.HttpClient
import io.ktor.websocket.Frame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriverWebSocketService @Inject constructor(
    client: HttpClient,
    preferencesManager: PreferencesManager
) : BaseWebSocketService(client, preferencesManager) {

    val _tripRequests = MutableStateFlow<List<TripRequestMessage>>(emptyList())
    val tripRequests: StateFlow<List<TripRequestMessage>> = _tripRequests

    private val _tripStatusUpdates = MutableStateFlow<TripStatusUpdateMessage?>(null)
    val tripStatusUpdates: StateFlow<TripStatusUpdateMessage?> = _tripStatusUpdates

    override fun getWebSocketPath(): String {
        return "/ws"
    }

    override fun handleIncomingMessage(messageText: String) {
        try {
            val json = Json { ignoreUnknownKeys = true }
            val baseMessage = json.decodeFromString<Map<String, JsonElement>>(messageText)
            val messageType = baseMessage["type"]?.jsonPrimitive?.content

            Log.d("DriverWebSocket", "Mensaje recibido tipo: $messageType")

            when (messageType) {
                "trip_request" -> {
                    val tripRequest = json.decodeFromString<TripRequestMessage>(messageText)
                    handleTripRequest(tripRequest)
                    Log.d("DriverWebSocket", "Solicitud de viaje recibida: ${tripRequest.data.tripId}")
                }
                "trip_status_update" -> {
                    // Aquí podrías manejar actualizaciones de estado de viaje
                    val statusUpdate = json.decodeFromString<TripStatusUpdateMessage>(messageText)
                    handleTripStatusUpdate(statusUpdate)
                    Log.d("DriverWebSocket", "Actualización de estado de viaje recibida: ${statusUpdate.data.tripId} - Estado: ${statusUpdate.data.status}")
                }
                // Otros tipos de mensajes que ya estaban manejando
            }
        } catch (e: Exception) {
            Log.e("DriverWebSocket", "Error procesando mensaje: ${e.message}")
        }
    }

    private fun handleTripRequest(tripRequest: TripRequestMessage) {
        scope.launch {
            // Añadir a la lista de solicitudes en lugar de reemplazar
            val currentRequests = _tripRequests.value
            // Verificar que la solicitud no esté ya en la lista
            if (!currentRequests.any { it.data.tripId == tripRequest.data.tripId }) {
                _tripRequests.value = currentRequests + tripRequest
                Log.d("DriverWebSocket", "Solicitud añadida. Total: ${_tripRequests.value.size}")
                // Aquí podrías implementar notificaciones, sonidos, etc.
            }
        }
    }

    private fun handleTripStatusUpdate(statudUpdate: TripStatusUpdateMessage) {
        scope.launch {
            _tripStatusUpdates.value = statudUpdate

            when(statudUpdate.data.status) {
                "started" -> Log.d("DriverWebSocket", "Viaje iniciado: ${statudUpdate.data.tripId}")
                "completed" -> Log.d("DriverWebSocket", "Viaje completado: ${statudUpdate.data.tripId}")
                "cancelled" -> Log.d("DriverWebSocket", "Viaje cancelado: ${statudUpdate.data.tripId}")
                else -> Log.w("DriverWebSocket", "Estado de viaje desconocido: ${statudUpdate.data.status}")
            }
        }
    }


    override fun connect() {
        super.connect()
        scope.launch {
            delay(1000)
            val user = preferencesManager.userData.value ?: return@launch
            sendMessage(
                WebSocketMessage(
                    type = "identify",
                    clientType = "driver",
                    userId = user.id,
                    driverData = WebSocketMessage.DriverData(
                        fullName = user.fullName,
                        location = WebSocketMessage.Location(
                            lat = user.latitude ?: 0.0,
                            lng = user.longitude ?: 0.0
                        ),
                        image = user.profileImage ?: "",
                        rating = user.rating
                    )
                )
            )
        }
    }


    fun updateLocation(latitude: Double, longitude: Double) {
        Log.d("DriverWebSocket", "Actualizando ubicación del conductor: lat=$latitude, lng=$longitude")

        // Guardar en PreferencesManager
        scope.launch {
            try {
                // Obtener datos actuales del usuario
                val currentUserData = preferencesManager.userData.value

                // Actualizar con la nueva ubicación
                currentUserData?.let {
                    val updatedUserData = it.copy(
                        latitude = latitude,
                        longitude = longitude
                    )
                    // Guardar datos actualizados
                    preferencesManager.saveUserData(updatedUserData)
                    Log.d("DriverWebSocket", "Ubicación guardada en preferencias: lat=$latitude, lng=$longitude")
                }
            } catch (e: Exception) {
                Log.e("DriverWebSocket", "Error al guardar ubicación en preferencias: ${e.message}")
            }
        }

        // Enviar a través de WebSocket
        val currentSession = session
        if (currentSession != null) {
            scope.launch {
                try {
                    val updateRequest = UpdateLocationRequest(
                        type = "update_location",
                        location = UpdateLocationRequest.Location(
                            lat = latitude,
                            lng = longitude
                        )
                    )

                    val messageJson = Json.encodeToString(updateRequest)
                    Log.d("DriverWebSocket", "Enviando actualización de ubicación: $messageJson")
                    currentSession.send(Frame.Text(messageJson))
                } catch (e: Exception) {
                    Log.e("DriverWebSocket", "Error al enviar actualización de ubicación: ${e.message}")
                    _connectionStatus.emit(ConnectionStatus.ERROR("Error al actualizar ubicación: ${e.message}"))
                }
            }
        } else {
            Log.w("DriverWebSocket", "No hay sesión activa para enviar actualización de ubicación")
        }
    }

    // En DriverWebSocketService.kt
    fun logout() {
        Log.d("DriverWebSocket", "Enviando mensaje de logout...")
        try {
            // Obtener el ID de usuario actual
            val userId = preferencesManager.userData.value?.id ?: 0
            // Enviar mensaje de desconexión
            sendMessage(
                WebSocketMessage(
                    type = "driver_logout",
                    userId = userId
                )
            )

            // Esperar un poco para asegurar que el mensaje se envía
            scope.launch {
                delay(300)
                Log.d("DriverWebSocket", "Desconectando WebSocket después de logout")
                disconnect() // Esto ya estaba en tu código
            }
        } catch (e: Exception) {
            Log.e("DriverWebSocket", "Error en logout: ${e.message}")
            disconnect() // Asegurar desconexión incluso si hay error
        }
    }

    fun resetTripStatusUpdates() {
        scope.launch {
            _tripStatusUpdates.value = null
        }
    }
}