package com.rodolfo.itaxcix.domain.repository

import com.rodolfo.itaxcix.domain.model.DriverAvailabilityResult
import com.rodolfo.itaxcix.domain.model.DriverStatusResult

interface DriverRepository {
    suspend fun getDriverStatus(userId: Int): DriverStatusResult
    suspend fun driverActivateAvailability(userId: Int): DriverAvailabilityResult
    suspend fun driverDeactivateAvailability(userId: Int): DriverAvailabilityResult
}