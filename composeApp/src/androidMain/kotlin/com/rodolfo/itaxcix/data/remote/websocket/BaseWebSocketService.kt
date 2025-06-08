package com.rodolfo.itaxcix.data.remote.websocket

import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.remote.api.ApiConfig
import com.rodolfo.itaxcix.data.remote.dto.WebSocketMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

abstract class BaseWebSocketService(
    protected val client: HttpClient,
    protected val preferencesManager: PreferencesManager
) {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    protected var session: DefaultClientWebSocketSession? = null

    private val _messages = MutableSharedFlow<WebSocketMessage>()
    val messages: SharedFlow<WebSocketMessage> = _messages.asSharedFlow()

    private val _connectionStatus = MutableSharedFlow<ConnectionStatus>()
    val connectionStatus: SharedFlow<ConnectionStatus> = _connectionStatus.asSharedFlow()

    protected abstract fun getWebSocketPath(): String

    fun connect() {
        if (session != null) return

        val userId = preferencesManager.userData.value?.id ?: return
        val token = preferencesManager.userData.value?.authToken ?: return

        scope.launch {
            try {
                _connectionStatus.emit(ConnectionStatus.CONNECTING)

                client.webSocket(
                    method = HttpMethod.Get,
                    host = ApiConfig.WS_HOST,
                    path = "${getWebSocketPath()}/$userId?token=$token"
                ) {
                    session = this
                    _connectionStatus.emit(ConnectionStatus.CONNECTED)

                    try {
                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    val text = frame.readText()
                                    val message = Json.decodeFromString<WebSocketMessage>(text)
                                    _messages.emit(message)
                                }
                                else -> {}
                            }
                        }
                    } catch (e: Exception) {
                        _connectionStatus.emit(ConnectionStatus.ERROR(e.message ?: "Error de conexión"))
                    } finally {
                        _connectionStatus.emit(ConnectionStatus.DISCONNECTED)
                        session = null
                    }
                }
            } catch (e: Exception) {
                _connectionStatus.emit(ConnectionStatus.ERROR(e.message ?: "Error al establecer conexión"))
                session = null
            }
        }
    }

    fun sendMessage(message: WebSocketMessage) {
        val currentSession = session
        if (currentSession != null) {
            scope.launch {
                try {
                    currentSession.send(Frame.Text(Json.encodeToString(message)))
                } catch (e: Exception) {
                    _connectionStatus.emit(ConnectionStatus.ERROR("Error al enviar mensaje: ${e.message}"))
                }
            }
        }
    }

    fun disconnect() {
        scope.launch {
            session?.close()
            session = null
            _connectionStatus.emit(ConnectionStatus.DISCONNECTED)
        }
    }
}

sealed class ConnectionStatus {
    object CONNECTING : ConnectionStatus()
    object CONNECTED : ConnectionStatus()
    object DISCONNECTED : ConnectionStatus()
    data class ERROR(val message: String) : ConnectionStatus()
}