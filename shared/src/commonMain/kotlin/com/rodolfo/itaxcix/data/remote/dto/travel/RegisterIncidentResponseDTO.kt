package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class RegisterIncidentResponseDTO (
    val message: String,
    val data: RegisterIncidentData
) {
    @Serializable
    data class RegisterIncidentData(
        val incidentId: Int,
        val message: String
    )
}