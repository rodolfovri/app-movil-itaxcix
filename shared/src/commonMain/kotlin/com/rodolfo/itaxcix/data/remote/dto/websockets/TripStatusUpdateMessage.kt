package com.rodolfo.itaxcix.data.remote.dto.websockets

import kotlinx.serialization.Serializable

@Serializable
data class TripStatusUpdateMessage (
    val type: String,
    val data: TripStatusUpdateData
) {
    @Serializable
    data class TripStatusUpdateData(
        val tripId: Int,
        val status: String, // e.g., "started", "completed", "cancelled"
    )
}