package com.rodolfo.itaxcix.domain.model

data class LoginResult(
    val message: String,
    val data: LoginData
) {
    data class LoginData(
        val token: String,
        val userId: Int,
        val documentValue: String,
        val firstName: String,
        val lastName: String,
        val roles: List<Role>,
        val permissions: List<Permission>,
        val rating: Double,
        val availabilityStatus: Boolean? = null
    ) {
        data class Role(
            val id: Int,
            val name: String
        )

        data class Permission(
            val id: Int,
            val name: String
        )
    }
}