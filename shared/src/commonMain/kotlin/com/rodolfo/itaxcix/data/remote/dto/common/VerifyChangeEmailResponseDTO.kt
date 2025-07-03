package com.rodolfo.itaxcix.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class VerifyChangeEmailResponseDTO (
    val message: String,
    val data: VerifyChangeEmailData
) {
    @Serializable
    data class VerifyChangeEmailData(
        val message: String
    )
}