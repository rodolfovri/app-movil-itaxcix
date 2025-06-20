package com.rodolfo.itaxcix.data.remote.api

object ApiConfig {

    const val BASE_URL = "https://149.130.161.148/api/v1"

    // Configuración WebSocket
    const val WS_HOST = "149.130.161.148" // Usa la misma IP que tu API
    const val WS_PATH = "ws"
    const val WS_PROTOCOL = "wss" // Protocolo seguro para WebSockets

    const val WS_URL_COMPLETED = "$WS_PROTOCOL://$WS_HOST/$WS_PATH"
}