package com.rodolfo.itaxcix.domain.model

data class User (
    val id: String,
    val nickname: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val city: String,
    val country: String,
)