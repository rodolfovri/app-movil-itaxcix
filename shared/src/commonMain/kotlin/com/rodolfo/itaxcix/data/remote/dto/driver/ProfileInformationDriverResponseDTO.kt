package com.rodolfo.itaxcix.data.remote.dto.driver

import kotlinx.serialization.Serializable

@Serializable
data class ProfileInformationDriverResponseDTO (
    val message: String,
    val data: ProfileInformationDriverDataDTO
) {
    @Serializable
    data class ProfileInformationDriverDataDTO(
        val firstName: String,
        val lastName: String,
        val documentType: String,
        val document: String,
        val email: String,
        val phone: String,
        val averageRating: Double,
        val ratingsCount: Int
    )
}