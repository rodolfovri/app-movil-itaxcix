package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class TravelResponseDTO (
    val message: String,
    val data: TravelData
) {
    @Serializable
    data class TravelData(
        val message: String,
        val travelId: Int
    )
}