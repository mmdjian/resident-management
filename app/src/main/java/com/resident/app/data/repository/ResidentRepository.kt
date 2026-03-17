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

    fun getByGender(gender: String): Flow<List<Resident>> = residentDao.getByGender(gender)

    fun getByEducation(education: String): Flow<List<Resident>> = residentDao.getByEducation(education)

    // 地址解析: 将查询字符串转换为 SQL LIKE 模式
    // 格式: 楼-单元-户号, 如 "1-1-101"
    // "0" 表示不确定, 如 "1-0-0" 表示 1号楼全部
    fun searchByBuilding(query: String): Flow<List<Resident>> {
        val parts = query.split("-").take(3).toMutableList()
        while (parts.size < 3) parts.add("0")

        val (building, unit, room) = parts.map { it.trim() }

        // 构建匹配模式
        val pattern = when {
            building == "0" && unit == "0" && room == "0" -> "%"
            building == "0" && unit == "0" -> "%-$room"
            building == "0" && room == "0" -> "%-$unit-%"
            building == "0" -> "%-$unit-$room%"
            unit == "0" && room == "0" -> "$building-%"
            unit == "0" -> "$building%-$room%"
            room == "0" -> "$building-$unit-%"
            else -> "$building-$unit-$room%"
        }

        return residentDao.searchByBuilding("%$pattern%")
    }

    fun searchResidents(query: String): Flow<List<Resident>> =
        residentDao.searchResidents("%$query%")

    fun searchAllFields(query: String): Flow<List<Resident>> =
        residentDao.searchAllFields("%$query%")

    fun searchByName(query: String): Flow<List<Resident>> =
        residentDao.searchByName("%$query%")

    fun searchByAddress(query: String): Flow<List<Resident>> =
        residentDao.searchByAddress("%$query%")

    suspend fun insertResident(resident: Resident): Long = residentDao.insertResident(resident)

    suspend fun updateResident(resident: Resident) = residentDao.updateResident(resident)

    suspend fun deleteResident(resident: Resident) = residentDao.deleteResident(resident)

    suspend fun getAllResidentsList(): List<Resident> = residentDao.getAllResidentsList()

    suspend fun getStatistics(): Statistics {
        val totalCount = residentDao.getResidentCount()
        val maleCount = residentDao.getMaleCount()
        val femaleCount = residentDao.getFemaleCount()
        val averageAge = residentDao.getAverageAge()
        val educationLevels = listOf("初中及以下", "高中/中专", "大专", "本科", "硕士及以上")
        val educationCounts = educationLevels.associateWith { residentDao.getEducationCount(it) }

        return Statistics(
            totalCount = totalCount,
            maleCount = maleCount,
            femaleCount = femaleCount,
            averageAge = averageAge?.toInt() ?: 0,
            educationCounts = educationCounts
        )
    }

    data class Statistics(
        val totalCount: Int,
        val maleCount: Int,
        val femaleCount: Int,
        val averageAge: Int,
        val educationCounts: Map<String, Int> = emptyMap()
    )
}
