package com.rodolfo.itaxcix.data.remote.dto.websockets

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketMessage(
    val type: String,
    val clientType: String? = null,
    val userId: Int? = null,
    val driverData: DriverData? = null,
    val citizenData: CitizenData? = null
) {
    @Serializable
    data class DriverData(
        val fullName: String,
        val location: Location,
        val image: String,
        val rating: Double
    )

    @Serializable
    data class CitizenData(
        val fullName: String,
        val location: Location,
        val image: String,
        val rating: Double
    )

    @Serializable
    data class Location(
        val lat: Double,
        val lng: Double
    )
}
