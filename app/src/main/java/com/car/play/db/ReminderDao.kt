package com.car.play.android.app.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReminderDao {
    @Insert suspend fun insert(reminder: ReminderEntity)
    @Update suspend fun update(reminder: ReminderEntity)
    @Delete suspend fun delete(reminder: ReminderEntity)
    @Query("SELECT * FROM reminders ORDER BY date ASC, time ASC") fun getAllReminders(): LiveData<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY date ASC") fun getActiveReminders(): LiveData<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE id = :id") fun getReminderById(id: Long): LiveData<ReminderEntity?>
    @Query("UPDATE reminders SET isCompleted = :completed WHERE id = :id") suspend fun updateCompletionStatus(id: Long, completed: Boolean)
}
