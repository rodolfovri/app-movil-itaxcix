package com.rodolfo.itaxcix.data.remote.websocket

import android.util.Log
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.remote.api.ApiConfig
import com.rodolfo.itaxcix.data.remote.dto.websockets.WebSocketMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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

    val _connectionStatus = MutableSharedFlow<ConnectionStatus>()
    val connectionStatus: SharedFlow<ConnectionStatus> = _connectionStatus.asSharedFlow()

    protected abstract fun getWebSocketPath(): String
    protected abstract fun handleIncomingMessage(messageText: String)

    open fun connect() {
        if (session != null) return

        scope.launch {
            try {
                _connectionStatus.emit(ConnectionStatus.CONNECTING)

                client.webSocket(
                    {
                        this.method = HttpMethod.Get
                        url(ApiConfig.WS_URL_COMPLETED) // Aquí usas la URL completa
                    },
                    block = {
                        session = this
                        _connectionStatus.emit(ConnectionStatus.CONNECTED)

                        try {
                            for (frame in incoming) {
                                when (frame) {
                                    is Frame.Text -> {
                                        val text = frame.readText()
                                        handleIncomingMessage(text)

                                        try {
                                            val message = Json { ignoreUnknownKeys = true }
                                                .decodeFromString<WebSocketMessage>(text)
                                            _messages.emit(message)
                                        } catch (e: Exception) {
                                            Log.d("WebSocket", "Mensaje no es WebSocketMessage estándar: ${e.message}")
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        } catch (e: Exception) {
                            _connectionStatus.emit(ConnectionStatus.ERROR(e.message ?: "Error de conexión"))
                            Log.d("WebSocket", "Error en el bucle de mensajes: ${e.message}")
                        } finally {
                            _connectionStatus.emit(ConnectionStatus.DISCONNECTED)
                            session = null
                        }
                    }
                )
            } catch (e: Exception) {
                Log.d("WebSocket", "Error al conectar: ${e.message}")
                _connectionStatus.emit(ConnectionStatus.ERROR(e.message ?: "Error al establecer conexión"))
                session = null
                reconnect() // Intentar reconectar después de un error
            }
        }
    }

    fun sendMessage(message: WebSocketMessage) {
        val currentSession = session
        if (currentSession != null) {
            scope.launch {
                try {
                    val messageJson = Json.encodeToString(message)
                    Log.d("WebSocket", "Enviando mensaje: $messageJson")
                    currentSession.send(Frame.Text(messageJson))
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error al enviar mensaje: ${e.message}")
                    _connectionStatus.emit(ConnectionStatus.ERROR("Error al enviar mensaje: ${e.message}"))
                }
            }
        }
    }

    private fun reconnect() {
        scope.launch {
            delay(5000) // Esperar 5 segundos antes de reconectar
            connect()
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