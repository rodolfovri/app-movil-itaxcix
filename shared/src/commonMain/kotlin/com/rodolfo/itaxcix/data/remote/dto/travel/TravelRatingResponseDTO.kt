package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class TravelRatingResponseDTO (
    val message: String,
    val data: TravelRatingDataDTO
) {
    @Serializable
    data class TravelRatingDataDTO(
        val driverRating: RatingDTO? = null,
        val citizenRating: RatingDTO? = null
    ) {
        @Serializable
        data class RatingDTO(
            val id: Int,
            val travelId: Int,
            val raterName: String,
            val ratedName: String,
            val score: Int,
            val comment: String,
            val createdAt: String
        )
    }
}