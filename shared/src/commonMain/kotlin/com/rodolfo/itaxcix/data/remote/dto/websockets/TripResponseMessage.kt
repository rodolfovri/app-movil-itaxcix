package com.rodolfo.itaxcix.data.remote.dto.websockets

import kotlinx.serialization.Serializable

@Serializable
data class TripResponseMessage (
    val type: String,
    val data: TripResponseData
) {
    @Serializable
    data class TripResponseData(
        val tripId: Int,
        val accepted: Boolean,
        val driverId: Int,
        val driverName: String,
        val estimatedArrival: Double
    )
}