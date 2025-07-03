package com.rodolfo.itaxcix.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class VerifyChangePhoneResponseDTO (
    val message: String,
    val data: VerifyChangePhoneData
) {
    @Serializable
    data class VerifyChangePhoneData(
        val message: String
    )
}