package com.rodolfo.itaxcix.domain.model

data class ProfileInformationCitizenResult (
    val firstName: String,
    val lastName: String,
    val documentType: String,
    val document: String,
    val email: String,
    val phone: String,
    val averageRating: Double,
    val ratingsCount: Int
)