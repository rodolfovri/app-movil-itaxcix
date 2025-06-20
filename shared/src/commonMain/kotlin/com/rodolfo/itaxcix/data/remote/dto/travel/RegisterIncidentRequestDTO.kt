package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class RegisterIncidentRequestDTO (
    val userId: Int,
    val travelId: Int,
    val typeName: String,
    val comment: String
)