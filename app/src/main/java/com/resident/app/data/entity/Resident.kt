package com.resident.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CustomFieldsConverter {
    @TypeConverter
    fun fromMap(map: Map<String, String>): String = Gson().toJson(map)

    @TypeConverter
    fun toMap(json: String): Map<String, String> {
        return try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            Gson().fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

@Entity(tableName = "residents")
@TypeConverters(CustomFieldsConverter::class)
data class Resident(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val gender: String = "",
    val birthDate: String = "",
    val age: Int = 0,
    val education: String = "",         // 受教育水平
    val occupation: String = "",
    val phone: String = "",
    val address: String = "",
    val customFields: Map<String, String> = emptyMap(),  // 自定义字段
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
