package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class TravelStartResponseDTO (
    val message: String,
    val data: TravelStartData
) {
    @Serializable
    data class TravelStartData(
        val message: String,
        val travelId: Int,
    )
}