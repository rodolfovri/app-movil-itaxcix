package com.rodolfo.itaxcix.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
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

            install(Logging) {
                level = LogLevel.ALL
            }
        }
    }
}