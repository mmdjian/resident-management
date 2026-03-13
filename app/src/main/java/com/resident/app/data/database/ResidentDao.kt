package com.resident.app.data.database

import androidx.room.*
import com.resident.app.data.entity.Resident
import kotlinx.coroutines.flow.Flow

@Dao
interface ResidentDao {
    @Query("SELECT * FROM residents ORDER BY createdAt DESC")
    fun getAllResidents(): Flow<List<Resident>>

    @Query("SELECT * FROM residents WHERE id = :id")
    suspend fun getResidentById(id: Long): Resident?

    @Query("SELECT * FROM residents WHERE name LIKE :searchQuery OR phone LIKE :searchQuery")
    fun searchResidents(searchQuery: String): Flow<List<Resident>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResident(resident: Resident): Long

    @Update
    suspend fun updateResident(resident: Resident)

    @Delete
    suspend fun deleteResident(resident: Resident)

    @Query("DELETE FROM residents")
    suspend fun deleteAllResidents()

    @Query("SELECT COUNT(*) FROM residents")
    suspend fun getResidentCount(): Int

    @Query("SELECT COUNT(*) FROM residents WHERE gender = '男'")
    suspend fun getMaleCount(): Int

    @Query("SELECT COUNT(*) FROM residents WHERE gender = '女'")
    suspend fun getFemaleCount(): Int

    @Query("SELECT AVG(age) FROM residents")
    suspend fun getAverageAge(): Double?
}
