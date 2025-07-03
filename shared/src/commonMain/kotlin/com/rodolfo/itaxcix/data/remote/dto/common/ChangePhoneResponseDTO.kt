package com.rodolfo.itaxcix.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class ChangePhoneResponseDTO (
    val message: String,
    val data: ChangePhoneData
) {
    @Serializable
    data class ChangePhoneData(
        val message: String
    )
}