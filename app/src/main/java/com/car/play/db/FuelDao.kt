package com.car.play.android.app.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FuelDao {

    @Insert
    suspend fun insert(entry: FuelEntryEntity)

    @Delete
    suspend fun delete(entry: FuelEntryEntity)

    @Query("SELECT * FROM fuel_entry ORDER BY odometer DESC, dateMillis DESC")
    fun getAll(): LiveData<List<FuelEntryEntity>>
}
