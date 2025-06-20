package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class TravelCancelResponseDTO (
    val message: String,
    val data: TravelCancelData
) {
    @Serializable
    data class TravelCancelData(
        val message: String,
        val travelId: Int,
    )
}