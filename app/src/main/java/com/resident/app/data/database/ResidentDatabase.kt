package com.resident.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.resident.app.data.entity.CustomFieldsConverter
import com.resident.app.data.entity.Memo
import com.resident.app.data.entity.Resident

@Database(
    entities = [Resident::class, Memo::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(CustomFieldsConverter::class)
abstract class ResidentDatabase : RoomDatabase() {
    abstract fun residentDao(): ResidentDao
    abstract fun memoDao(): MemoDao

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
