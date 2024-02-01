package com.example.work_manager_test.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,
    entities = [
        ReminderEntity::class
    ]
)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun getReminderDao(): ReminderDao
}