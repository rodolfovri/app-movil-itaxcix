package com.rodolfo.itaxcix.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: String? = null,
    val nickname: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
)