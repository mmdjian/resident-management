package com.resident.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "residents")
data class Resident(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val gender: String = "",
    val birthDate: String = "",
    val age: Int = 0,
    val occupation: String = "",
    val phone: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
