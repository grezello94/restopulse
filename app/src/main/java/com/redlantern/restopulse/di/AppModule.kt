package com.redlantern.restopulse.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.redlantern.restopulse.data.database.RestoPulseDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): RestoPulseDatabase =
        Room.databaseBuilder(context, RestoPulseDatabase::class.java, "restopulse.db")
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides fun provideCustomerDao(db: RestoPulseDatabase) = db.customerDao()
    @Provides fun provideCallHistoryDao(db: RestoPulseDatabase) = db.callHistoryDao()
    @Provides fun provideMarketingGroupDao(db: RestoPulseDatabase) = db.marketingGroupDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
