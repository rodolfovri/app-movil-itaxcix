package com.rodolfo.itaxcix.domain.model

data class TravelRatingResult (
    val message: String,
    val driverRating: Rating? = null,
    val citizenRating: Rating? = null
) {
    data class Rating(
        val id: Int,
        val travelId: Int,
        val raterName: String,
        val ratedName: String,
        val score: Int,
        val comment: String,
        val createdAt: String
    )
}