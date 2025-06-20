package com.rodolfo.itaxcix.data.remote.dto.websockets

import kotlinx.serialization.Serializable

@Serializable
data class DriverLocationUpdateResponse(
    val type: String,
    val data: DriverData
) {
    @Serializable
    data class DriverData(
        val id: Int,
        val location: Location
    )

    @Serializable
    data class Location(
        val lat: Double,
        val lng: Double
    )
}