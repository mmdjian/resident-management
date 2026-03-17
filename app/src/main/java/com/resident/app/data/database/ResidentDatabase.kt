package com.resident.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.resident.app.data.entity.Resident

@Database(
    entities = [Resident::class],
    version = 3,
    exportSchema = false
)
abstract class ResidentDatabase : RoomDatabase() {
    abstract fun residentDao(): ResidentDao

    companion object {
        @Volatile
        private var INSTANCE: ResidentDatabase? = null

        fun getDatabase(context: Context): ResidentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ResidentDatabase::class.java,
                    "resident_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
