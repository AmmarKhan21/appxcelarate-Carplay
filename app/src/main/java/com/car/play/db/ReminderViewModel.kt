package com.car.play.android.app.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val reminderDao = AppDatabase.getDatabase(application).reminderDao()
    val allReminders: LiveData<List<ReminderEntity>> = reminderDao.getAllReminders()
    val activeReminders: LiveData<List<ReminderEntity>> = reminderDao.getActiveReminders()

    fun addReminder(title: String, description: String, date: String, time: String, isRecurring: Boolean, recurringInterval: String, category: String, priority: String) {
        viewModelScope.launch {
            reminderDao.insert(ReminderEntity(title = title, description = description, date = date, time = time, isRecurring = isRecurring, recurringInterval = recurringInterval, isCompleted = false, category = category, priority = priority))
        }
    }
    fun deleteReminder(reminder: ReminderEntity) { viewModelScope.launch { reminderDao.delete(reminder) } }
    fun markComplete(id: Long, completed: Boolean) { viewModelScope.launch { reminderDao.updateCompletionStatus(id, completed) } }
}
