package com.rodolfo.itaxcix.domain.model

data class GetProfilePhotoResult (
    val message: String,
    val base64Image : String? = null
)