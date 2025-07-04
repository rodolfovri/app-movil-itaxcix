package com.rodolfo.itaxcix.domain.repository

import com.rodolfo.itaxcix.data.remote.dto.driver.DriverToCitizenRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.driver.VehicleAssociationRequestDTO
import com.rodolfo.itaxcix.domain.model.DriverToCitizenResult
import com.rodolfo.itaxcix.domain.model.DriverAvailabilityResult
import com.rodolfo.itaxcix.domain.model.DriverStatusResult
import com.rodolfo.itaxcix.domain.model.ProfileInformationDriverResult
import com.rodolfo.itaxcix.domain.model.TravelRespondResult
import com.rodolfo.itaxcix.domain.model.TravelStartResult
import com.rodolfo.itaxcix.domain.model.VehicleAssociationResult
import com.rodolfo.itaxcix.domain.model.VehicleDisassociationResult

interface DriverRepository {
    suspend fun getDriverStatus(userId: Int): DriverStatusResult
    suspend fun toggleDriverAvailability(driverId: Int): DriverAvailabilityResult
    suspend fun profileInformationDriver(userId: Int): ProfileInformationDriverResult
    suspend fun travelRespond(travelId: Int, accept: Boolean): TravelRespondResult
    suspend fun travelStart(travelId: Int): TravelStartResult
    suspend fun driverToCitizen(driverToCitizen: DriverToCitizenRequestDTO): DriverToCitizenResult
    suspend fun vehicleAssociation(userId: Int, vehicle: VehicleAssociationRequestDTO): VehicleAssociationResult
    suspend fun vehicleDissociation(userId: Int): VehicleDisassociationResult
}