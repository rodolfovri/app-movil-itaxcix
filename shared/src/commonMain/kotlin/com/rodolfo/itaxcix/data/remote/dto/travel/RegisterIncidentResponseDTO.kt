package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class RegisterIncidentResponseDTO (
    val incidentId: Int,
    val message: String
)