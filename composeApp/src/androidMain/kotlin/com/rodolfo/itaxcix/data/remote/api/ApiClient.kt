package com.rodolfo.itaxcix.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ApiClient {
    fun create(): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                config {
                    // Configurar timeouts en OkHttp
                    connectTimeout(30, TimeUnit.SECONDS)
                    readTimeout(30, TimeUnit.SECONDS)
                    writeTimeout(30, TimeUnit.SECONDS)
                    retryOnConnectionFailure(true)
                }
            }

            // Plugin de timeout de Ktor
            install(HttpTimeout) {
                requestTimeoutMillis = 30000  // 30 segundos
                connectTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }

            install(WebSockets) {
                // Configuración de WebSockets si es necesario
                pingInterval = 15_000 // Intervalo de ping para mantener la conexión viva
                maxFrameSize = Long.MAX_VALUE // Tamaño máximo del frame
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }

            install(Logging) {
                level = LogLevel.HEADERS
                // Personalizar el logger para ocultar información sensible
                logger = object : Logger {
                    override fun log(message: String) {
                        // Reemplazar URLs y datos sensibles en los logs
                        val sanitizedMessage = message.replace(
                            Regex("https?://[^\\s/]+"),
                            "[URL_PROTEGIDA]"
                        )
                        println("KTOR_CLIENT: $sanitizedMessage")
                    }
                }
            }

            // Observador para manejar respuestas inesperadas
            install(ResponseObserver) {
                onResponse { response ->
                    if (response.status != HttpStatusCode.OK ||
                        response.headers["Content-Type"]?.contains("text/html") == true) {
                        println("KTOR_CLIENT: Tipo de respuesta inesperado - Estado: ${response.status}")
                    }
                }
            }

            expectSuccess = false // No lanzar excepciones por respuestas no 2xx
        }
    }
}