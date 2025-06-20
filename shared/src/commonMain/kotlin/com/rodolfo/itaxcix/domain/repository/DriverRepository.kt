package com.rodolfo.itaxcix.domain.repository

import com.rodolfo.itaxcix.domain.model.DriverAvailabilityResult
import com.rodolfo.itaxcix.domain.model.DriverStatusResult
import com.rodolfo.itaxcix.domain.model.TravelRespondResult
import com.rodolfo.itaxcix.domain.model.TravelStartResult

interface DriverRepository {
    suspend fun getDriverStatus(userId: Int): DriverStatusResult
    suspend fun toggleDriverAvailability(driverId: Int): DriverAvailabilityResult
    suspend fun travelRespond(travelId: Int, accept: Boolean): TravelRespondResult
    suspend fun travelStart(travelId: Int): TravelStartResult
}