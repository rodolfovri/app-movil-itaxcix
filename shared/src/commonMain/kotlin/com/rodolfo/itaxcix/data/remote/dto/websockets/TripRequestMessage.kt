package com.rodolfo.itaxcix.data.remote.dto.websockets

import kotlinx.serialization.Serializable

@Serializable
data class TripRequestMessage (
    val type: String,
    val data: TripRequestData
) {
    @Serializable
    data class TripRequestData(
        val tripId: Int,
        val passengerId: Int,
        val origin: Location,
        val destination: Location,
        val passengerName: String,
        val passengerRating: Double,
        val price: Double = 0.0,  // Ahora tiene valor predeterminado
        val distance: Double = 0.0  // Ahora tiene valor predeterminado
    )

    @Serializable
    data class Location(
        val lat: Double,
        val lng: Double
    )
}