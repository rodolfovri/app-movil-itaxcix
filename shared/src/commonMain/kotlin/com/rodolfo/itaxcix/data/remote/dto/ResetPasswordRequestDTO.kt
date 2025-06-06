package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequestDTO (
    val userId: Int,
    val newPassword: String,
    val repeatPassword: String,
)