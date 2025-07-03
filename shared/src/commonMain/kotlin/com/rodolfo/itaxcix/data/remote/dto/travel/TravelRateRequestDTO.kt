package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class TravelRateRequestDTO (
    val travelId: Int,
    val raterId: Int,
    val score: Int,
    val comment: String
)