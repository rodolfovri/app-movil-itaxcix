package com.rodolfo.itaxcix.data.remote.dto.travel

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyNumberResponseDTO (
    val message: String,
    val data: EmergencyNumberData
) {
    @Serializable
    data class EmergencyNumberData(
        val number: String
    )
}