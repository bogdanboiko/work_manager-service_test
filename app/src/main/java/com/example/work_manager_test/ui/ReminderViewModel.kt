package com.example.work_manager_test.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.work_manager_test.data.ReminderDao
import com.example.work_manager_test.data.ReminderEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(private val reminderDao: ReminderDao) : ViewModel() {
    var date: Calendar? = null
    var time: Calendar? = null

    private val _reminderEvents = MutableSharedFlow<ReminderEvents>()
    val reminderEvents = _reminderEvents.asSharedFlow()

    private val _reminders = MutableStateFlow<List<ReminderEntity>>(emptyList())
    val reminders = _reminders.asStateFlow()

    init {
        collectRemindersList()
    }

    fun sendReminderEvent(event: ReminderEvents) {
        viewModelScope.launch(Dispatchers.IO) {
            _reminderEvents.emit(event)
        }
    }

    fun cancelReminder(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.deleteReminder(id)
        }
    }

    private fun collectRemindersList() {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.getReminders().collect {
                _reminders.emit(it)
            }
        }
    }

    fun createReminder(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (date != null && time != null) {
                val reminder = ReminderEntity(
                    UUID.randomUUID().toString(),
                    dateFormatter.format(date!!.time),
                    timeFormatter.format(time!!.time),
                    message
                )
                reminderDao.updateReminderEntity(reminder)
                _reminderEvents.emit(ReminderEvents.ReminderAddedSuccessfully(reminder))
            }
        }
    }

    fun updateReminderDate(date: Calendar, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.updateReminderDate(dateFormatter.format(date.time), id)
            val updatedReminder = reminders.value.find { it.id == id }

            if (updatedReminder != null) {
                _reminderEvents.emit(ReminderEvents.UpdateReminder(updatedReminder))
            }
        }
    }

    fun updateReminderTime(time: Calendar, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.updateReminderTime(timeFormatter.format(time.time), id)
            val updatedReminder = reminders.value.find { it.id == id }

            if (updatedReminder != null) {
                _reminderEvents.emit(ReminderEvents.UpdateReminder(updatedReminder))
            }
        }
    }

    companion object {
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
        val timeFormatter = SimpleDateFormat("HH:mm")
        val fullDateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm")
    }
}