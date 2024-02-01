package com.example.work_manager_test.di

import android.content.Context
import androidx.room.Room
import com.example.work_manager_test.data.ReminderDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, ReminderDatabase::class.java, "reminder_database")
            .fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideReminderDao(reminderDatabase: ReminderDatabase) = reminderDatabase.getReminderDao()
}