package com.resident.app.data.repository

import com.resident.app.data.database.ResidentDao
import com.resident.app.data.entity.Resident
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResidentRepository @Inject constructor(
    private val residentDao: ResidentDao
) {
    fun getAllResidents(): Flow<List<Resident>> = residentDao.getAllResidents()

    suspend fun getResidentById(id: Long): Resident? = residentDao.getResidentById(id)

    fun searchResidents(query: String): Flow<List<Resident>> =
        residentDao.searchResidents("%$query%")

    suspend fun insertResident(resident: Resident): Long = residentDao.insertResident(resident)

    suspend fun updateResident(resident: Resident) = residentDao.updateResident(resident)

    suspend fun deleteResident(resident: Resident) = residentDao.deleteResident(resident)

    suspend fun getStatistics(): Statistics {
        val totalCount = residentDao.getResidentCount()
        val maleCount = residentDao.getMaleCount()
        val femaleCount = residentDao.getFemaleCount()
        val averageAge = residentDao.getAverageAge()

        return Statistics(
            totalCount = totalCount,
            maleCount = maleCount,
            femaleCount = femaleCount,
            averageAge = averageAge?.toInt() ?: 0
        )
    }

    data class Statistics(
        val totalCount: Int,
        val maleCount: Int,
        val femaleCount: Int,
        val averageAge: Int
    )
}
