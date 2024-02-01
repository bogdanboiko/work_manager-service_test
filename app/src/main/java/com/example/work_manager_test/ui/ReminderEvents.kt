package com.example.work_manager_test.ui

import com.example.work_manager_test.data.ReminderEntity

sealed class ReminderEvents {
    data class ReminderAddedSuccessfully(val reminderEntity: ReminderEntity): ReminderEvents()
    data class CancelReminder(val id: String): ReminderEvents()
    data class UpdateReminder(val updatedReminder: ReminderEntity): ReminderEvents()
}