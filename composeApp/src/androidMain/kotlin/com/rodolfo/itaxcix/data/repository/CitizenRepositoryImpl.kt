package com.rodolfo.itaxcix.data.repository

import com.rodolfo.itaxcix.data.remote.api.ApiService
import com.rodolfo.itaxcix.data.remote.dto.citizen.CitizenToDriverRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRequestDTO
import com.rodolfo.itaxcix.domain.model.CitizenToDriverResult
import com.rodolfo.itaxcix.domain.model.ProfileInformationCitizenResult
import com.rodolfo.itaxcix.domain.model.TravelResult
import com.rodolfo.itaxcix.domain.repository.CitizenRepository

class CitizenRepositoryImpl (private val apiService: ApiService) : CitizenRepository {

    override suspend fun travels(travel: TravelRequestDTO): TravelResult {
        val response = apiService.travels(travel)
        return TravelResult(
            message = response.message,
            travelId = response.data.travelId
        )
    }

    override suspend fun profileInformationCitizen(userId: Int): ProfileInformationCitizenResult{
        val response = apiService.profileInformationCitizen(userId)
        return ProfileInformationCitizenResult(
            firstName = response.data.firstName,
            lastName = response.data.lastName,
            documentType = response.data.documentType,
            document = response.data.document,
            email = response.data.email,
            phone = response.data.phone,
            averageRating = response.data.averageRating,
            ratingsCount = response.data.ratingsCount
        )
    }

    override suspend fun citizenToDriver(citizenToDriver: CitizenToDriverRequestDTO): CitizenToDriverResult {
        val response = apiService.citizenToDriver(citizenToDriver)
        return CitizenToDriverResult(
            userId = response.data.userId,
            status = response.data.status,
            message = response.data.message,
            driverProfileId = response.data.driverProfileId
        )
    }
}