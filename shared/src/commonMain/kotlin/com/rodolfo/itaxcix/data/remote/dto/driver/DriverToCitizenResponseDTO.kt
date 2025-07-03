package com.rodolfo.itaxcix.data.remote.dto.driver

import kotlinx.serialization.Serializable

@Serializable
data class DriverToCitizenResponseDTO (
    val data: DriverToCitizenData
) {
    @Serializable
    data class DriverToCitizenData(
        val userId: Int,
        val status: String,
        val message: String,
        val citizenProfileId: Int
    )
}