package com.car.play.android.app.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TirePressureDao {
    @Insert suspend fun insert(tirePressure: TirePressureEntity)
    @Update suspend fun update(tirePressure: TirePressureEntity)
    @Delete suspend fun delete(tirePressure: TirePressureEntity)
    @Query("SELECT * FROM tire_pressure ORDER BY date DESC") fun getAllRecords(): LiveData<List<TirePressureEntity>>
    @Query("SELECT * FROM tire_pressure ORDER BY date DESC LIMIT 1") fun getLatestRecord(): LiveData<TirePressureEntity?>
}
