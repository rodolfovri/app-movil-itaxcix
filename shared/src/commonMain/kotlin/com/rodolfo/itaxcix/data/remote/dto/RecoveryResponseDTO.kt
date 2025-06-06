package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecoveryResponseDTO (
    val message: String = "",
    val data: RecoveryDataDTO
) {
    @Serializable
    data class RecoveryDataDTO(
        val userId: Int = 0
    )
}