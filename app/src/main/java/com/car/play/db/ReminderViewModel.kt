package com.car.play.android.app.db

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.car.play.android.app.services.ReminderScheduler
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val reminderDao = AppDatabase.getDatabase(application).reminderDao()
    val allReminders: LiveData<List<ReminderEntity>> = reminderDao.getAllReminders()
    val activeReminders: LiveData<List<ReminderEntity>> = reminderDao.getActiveReminders()

    fun addReminder(
        title: String,
        description: String,
        date: String,
        time: String,
        isRecurring: Boolean,
        recurringInterval: String,
        category: String,
        priority: String
    ) {
        viewModelScope.launch {
            try {
                val id = reminderDao.insert(
                    ReminderEntity(
                        title = title,
                        description = description,
                        date = date,
                        time = time,
                        isRecurring = isRecurring,
                        recurringInterval = recurringInterval,
                        isCompleted = false,
                        category = category,
                        priority = priority
                    )
                )
                ReminderScheduler.scheduleReminder(
                    getApplication(),
                    ReminderEntity(
                        id = id,
                        title = title,
                        description = description,
                        date = date,
                        time = time,
                        isRecurring = isRecurring,
                        recurringInterval = recurringInterval,
                        isCompleted = false,
                        category = category,
                        priority = priority
                    )
                )
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Failed to save reminder", e)
            }
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            try {
                ReminderScheduler.cancelReminder(getApplication(), reminder.id)
                reminderDao.delete(reminder)
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Failed to delete reminder", e)
            }
        }
    }

    fun markComplete(id: Long, completed: Boolean) {
        viewModelScope.launch {
            try {
                if (completed) {
                    ReminderScheduler.cancelReminder(getApplication(), id)
                }
                reminderDao.updateCompletionStatus(id, completed)
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Failed to update reminder", e)
            }
        }
    }

    fun rescheduleActiveReminders() {
        viewModelScope.launch {
            try {
                ReminderScheduler.rescheduleAll(getApplication(), reminderDao.getActiveRemindersSync())
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Failed to reschedule reminders", e)
            }
        }
    }
}
