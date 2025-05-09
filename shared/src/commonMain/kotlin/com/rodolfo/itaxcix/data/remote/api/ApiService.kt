package com.rodolfo.itaxcix.data.remote.api

import com.rodolfo.itaxcix.data.remote.dto.UserDTO
import com.rodolfo.itaxcix.data.remote.dto.CitizenRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.DriverRegisterRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterDriverResponseDTO
import com.rodolfo.itaxcix.data.remote.dto.RegisterResponseDTO

interface ApiService {
    suspend fun getUsers(): List<UserDTO>
    suspend fun getUserById(id: String): UserDTO
    suspend fun registerCitizen(citizen: CitizenRegisterRequestDTO): RegisterResponseDTO
    suspend fun registerDriver(driver: DriverRegisterRequestDTO): RegisterDriverResponseDTO
    suspend fun login(username: String, password: String): UserDTO
}