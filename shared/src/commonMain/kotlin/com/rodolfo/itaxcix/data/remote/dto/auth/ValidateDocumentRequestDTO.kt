package com.rodolfo.itaxcix.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ValidateDocumentRequestDTO (
    val documentTypeId: Int,
    val documentValue: String
)