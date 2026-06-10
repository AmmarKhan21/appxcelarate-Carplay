package com.car.play.android.app.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val reminderDao = AppDatabase.getDatabase(application).reminderDao()
    val allReminders: LiveData<List<ReminderEntity>> = reminderDao.getAll()

    fun addReminder(
        title: String,
        type: String,
        dueDateMillis: Long,
        note: String,
        leadDays: Int
    ) {
        viewModelScope.launch {
            reminderDao.insert(
                ReminderEntity(
                    title = title,
                    type = type,
                    dueDateMillis = dueDateMillis,
                    note = note,
                    leadDays = leadDays,
                    notified = false
                )
            )
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch { reminderDao.delete(reminder) }
    }
}
