package com.rodolfo.itaxcix.data.remote.dto.websockets

import kotlinx.serialization.Serializable

@Serializable
data class DriverUnavailableResponse(
    val type: String,
    val data: DriverData
) {
    @Serializable
    data class DriverData(
        val id: Int
    )
}