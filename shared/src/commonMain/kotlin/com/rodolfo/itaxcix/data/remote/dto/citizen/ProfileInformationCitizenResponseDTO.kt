package com.rodolfo.itaxcix.data.remote.dto.citizen

import kotlinx.serialization.Serializable

@Serializable
data class ProfileInformationCitizenResponseDTO (
    val message: String,
    val data: ProfileInformationCitizenDataDTO
) {
    @Serializable
    data class ProfileInformationCitizenDataDTO(
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