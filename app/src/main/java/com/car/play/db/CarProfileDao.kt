package com.car.play.android.app.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CarProfileDao {
    @Insert suspend fun insert(car: CarProfileEntity)
    @Update suspend fun update(car: CarProfileEntity)
    @Delete suspend fun delete(car: CarProfileEntity)
    @Query("SELECT * FROM car_profiles ORDER BY name ASC") fun getAllCars(): LiveData<List<CarProfileEntity>>
    @Query("SELECT * FROM car_profiles WHERE isActive = 1 LIMIT 1") fun getActiveCar(): LiveData<CarProfileEntity?>
    @Query("UPDATE car_profiles SET isActive = 0") suspend fun deactivateAll()
    @Query("UPDATE car_profiles SET isActive = 1 WHERE id = :id") suspend fun setActiveCar(id: Long)
}
