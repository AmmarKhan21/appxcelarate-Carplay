package com.car.play.android.app.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TripDao {
    @Insert suspend fun insert(trip: TripEntity)
    @Update suspend fun update(trip: TripEntity)
    @Delete suspend fun delete(trip: TripEntity)
    @Query("SELECT * FROM trips ORDER BY startTime DESC") fun getAllTrips(): LiveData<List<TripEntity>>
    @Query("SELECT SUM(distance) FROM trips") fun getTotalDistance(): LiveData<Double?>
    @Query("SELECT COUNT(*) FROM trips") fun getTotalTrips(): LiveData<Int?>
    @Query("SELECT * FROM trips ORDER BY startTime DESC LIMIT 1") fun getLastTrip(): LiveData<TripEntity?>
}
