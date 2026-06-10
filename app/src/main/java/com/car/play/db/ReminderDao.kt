package com.car.play.android.app.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReminderDao {

    @Insert
    suspend fun insert(reminder: ReminderEntity)

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("SELECT * FROM car_reminder ORDER BY dueDateMillis ASC")
    fun getAll(): LiveData<List<ReminderEntity>>

    @Query("SELECT * FROM car_reminder ORDER BY dueDateMillis ASC")
    suspend fun getAllOnce(): List<ReminderEntity>

    @Query("UPDATE car_reminder SET notified = 1 WHERE id = :id")
    suspend fun markNotified(id: Long)
}
