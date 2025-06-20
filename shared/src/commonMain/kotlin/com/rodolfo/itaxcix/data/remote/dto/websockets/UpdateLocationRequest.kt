package com.rodolfo.itaxcix.data.remote.dto.websockets

import kotlinx.serialization.Serializable

@Serializable
data class UpdateLocationRequest (
    val type: String,
    val location: Location
) {
    @Serializable
    data class Location(
        val lat: Double,
        val lng: Double
    )
}