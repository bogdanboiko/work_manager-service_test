package com.example.work_manager_test.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReminderEntity(
    @PrimaryKey
    val id: String,
    val date: String,
    val time: String,
    val message: String
)