package com.example.work_manager_test.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updateReminderEntity(reminderEntity: ReminderEntity)

    @Query("SELECT * FROM ReminderEntity")
    abstract fun getReminders(): Flow<List<ReminderEntity>>

    @Query("DELETE FROM ReminderEntity WHERE id = :id")
    abstract suspend fun deleteReminder(id: String)

    @Query("UPDATE ReminderEntity SET date = :date WHERE id = :id")
    abstract suspend fun updateReminderDate(date: String, id: String)

    @Query("UPDATE ReminderEntity SET time = :time WHERE id = :id")
    abstract suspend fun updateReminderTime(time: String, id: String)
}