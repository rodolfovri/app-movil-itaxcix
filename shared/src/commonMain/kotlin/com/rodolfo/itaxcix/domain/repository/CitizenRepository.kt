package com.rodolfo.itaxcix.domain.repository

import com.rodolfo.itaxcix.data.remote.dto.citizen.CitizenToDriverRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRequestDTO
import com.rodolfo.itaxcix.domain.model.CitizenToDriverResult
import com.rodolfo.itaxcix.domain.model.ProfileInformationCitizenResult
import com.rodolfo.itaxcix.domain.model.TravelResult

interface CitizenRepository {
    suspend fun travels(travel: TravelRequestDTO): TravelResult
    suspend fun profileInformationCitizen(userId: Int): ProfileInformationCitizenResult
    suspend fun citizenToDriver(citizenToDriver: CitizenToDriverRequestDTO): CitizenToDriverResult
}