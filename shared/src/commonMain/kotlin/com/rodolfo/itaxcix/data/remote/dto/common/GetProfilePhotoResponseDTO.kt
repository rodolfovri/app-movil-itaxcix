package com.rodolfo.itaxcix.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class GetProfilePhotoResponseDTO (
    val message: String,
    val data: GetProfilePhotoDataDTO
) {
    @Serializable
    data class GetProfilePhotoDataDTO(
        val userId: Int,
        val base64Image: String? = null
    )
}