package com.rodolfo.itaxcix.data.repository

import com.rodolfo.itaxcix.data.remote.api.ApiService
import com.rodolfo.itaxcix.domain.model.DriverAvailabilityResult
import com.rodolfo.itaxcix.domain.model.DriverStatusResult
import com.rodolfo.itaxcix.domain.repository.DriverRepository

class DriverRepositoryImpl(private val apiService: ApiService) : DriverRepository {

    override suspend fun getDriverStatus(userId: Int): DriverStatusResult {
        val response = apiService.getDriverStatus(userId)
        return DriverStatusResult(
            isDriverAvailable = response.available,
            lastDriverStatusUpdate = response.lastUpdated
        )
    }

    override suspend fun toggleDriverAvailability(driverId: Int): DriverAvailabilityResult {
        val response = apiService.toggleDriverAvailability(driverId)
        return DriverAvailabilityResult(
            message = response.message,
            available = response.data.available
        )
    }
}