package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketMessage (
    val type: String,
    val data: Map<String, String> = emptyMap(),
)