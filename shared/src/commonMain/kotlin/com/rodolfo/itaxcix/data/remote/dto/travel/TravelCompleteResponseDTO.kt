package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class TravelCompleteResponseDTO (
    val message: String,
    val data: TravelCompleteDataDTO
) {
    @Serializable
    data class TravelCompleteDataDTO(
        val message: String,
        val travelId: Int
    )
}