package com.rodolfo.itaxcix.data.remote.dto.websockets

import kotlinx.serialization.Serializable

@Serializable
data class InitialDriversResponse(
    val type: String,
    val drivers: List<DriverInfo>
) {
    @Serializable
    data class DriverInfo(
        val id: Int,
        val fullName: String,
        val image: String,
        val location: Location,
        val rating: Double,
        val timestamp: Long
    )

    @Serializable
    data class Location(
        val lat: Double,
        val lng: Double
    )
}