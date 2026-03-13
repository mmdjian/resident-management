package com.resident.app.di

import android.content.Context
import com.resident.app.data.database.ResidentDao
import com.resident.app.data.database.ResidentDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ResidentDatabase {
        return ResidentDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideResidentDao(database: ResidentDatabase): ResidentDao {
        return database.residentDao()
    }
}
