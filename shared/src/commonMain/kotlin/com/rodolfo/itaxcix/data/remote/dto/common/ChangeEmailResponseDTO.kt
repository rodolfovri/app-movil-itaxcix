package com.rodolfo.itaxcix.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class ChangeEmailResponseDTO (
    val message: String,
    val data: ChangeEmailData
) {
    @Serializable
    data class ChangeEmailData(
        val message: String
    )
}