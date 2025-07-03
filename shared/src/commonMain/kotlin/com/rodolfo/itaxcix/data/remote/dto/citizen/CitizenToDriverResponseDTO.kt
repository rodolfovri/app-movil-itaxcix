package com.rodolfo.itaxcix.data.remote.dto.citizen

import kotlinx.serialization.Serializable

@Serializable
data class CitizenToDriverResponseDTO (
    val data: CitizenToDriverData
) {
    @Serializable
    data class CitizenToDriverData(
        val userId: Int,
        val status: String,
        val message: String,
        val driverProfileId: Int
    )
}