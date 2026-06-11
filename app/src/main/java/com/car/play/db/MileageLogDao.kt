package com.car.play.android.app.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MileageLogDao {
    @Insert suspend fun insert(log: MileageLogEntity)
    @Update suspend fun update(log: MileageLogEntity)
    @Delete suspend fun delete(log: MileageLogEntity)
    @Query("SELECT * FROM mileage_logs ORDER BY date DESC") fun getAllLogs(): LiveData<List<MileageLogEntity>>
    @Query("SELECT SUM(distance) FROM mileage_logs WHERE isBusinessTrip = 1") fun getTotalBusinessMileage(): LiveData<Double?>
    @Query("SELECT SUM(distance) FROM mileage_logs") fun getTotalMileage(): LiveData<Double?>
}
