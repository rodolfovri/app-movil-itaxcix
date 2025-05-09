package com.rodolfo.itaxcix.domain.repository

import com.rodolfo.itaxcix.data.remote.dto.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterResponseDTO
import com.rodolfo.itaxcix.domain.model.RegisterDriverResult
import com.rodolfo.itaxcix.domain.model.RegisterResult
import com.rodolfo.itaxcix.domain.model.User

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: String): User
    suspend fun registerCitizen(user: CitizenRegisterRequestDTO): RegisterResult
    suspend fun registerDriver(user: DriverRegisterRequestDTO): RegisterDriverResult
    suspend fun login(username: String, password: String): User
}