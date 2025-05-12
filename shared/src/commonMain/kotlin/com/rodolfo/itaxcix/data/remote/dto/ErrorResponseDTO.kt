package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDTO (
    val error: ErrorDetail
) {
    @Serializable
    data class ErrorDetail(
        val code: String,
        val message: String,
        val status: Int
    )
}