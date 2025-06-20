package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ValidateDocumentResponseDTO (
    val message: String = "",
    val data : ValidateDocumentDataDTO? = null
) {
    @Serializable
    data class ValidateDocumentDataDTO(
        val personId: Int,
    )
}