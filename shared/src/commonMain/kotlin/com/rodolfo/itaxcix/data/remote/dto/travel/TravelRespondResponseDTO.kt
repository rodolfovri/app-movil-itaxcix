package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class TravelRespondResponseDTO (
    val message: String,
    val data: TravelRespondData
) {
    @Serializable
    data class TravelRespondData(
        val message: String,
        val travelId: Int
    )
}