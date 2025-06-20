package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class TravelRespondRequestDTO (
    val travelId: Int,
    val accept: Boolean
)