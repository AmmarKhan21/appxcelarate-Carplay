package com.car.play.android.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val isRecurring: Boolean,
    val recurringInterval: String,
    val isCompleted: Boolean,
    val category: String,
    val priority: String
)
