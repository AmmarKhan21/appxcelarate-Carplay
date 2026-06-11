package com.car.play.android.app.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FuelDao {
    @Insert suspend fun insert(fuel: FuelEntity)
    @Update suspend fun update(fuel: FuelEntity)
    @Delete suspend fun delete(fuel: FuelEntity)
    @Query("SELECT * FROM fuel_records ORDER BY date DESC") fun getAllFuelRecords(): LiveData<List<FuelEntity>>
    @Query("SELECT SUM(totalCost) FROM fuel_records") fun getTotalFuelCost(): LiveData<Double?>
    @Query("SELECT AVG(totalCost/liters) FROM fuel_records") fun getAverageCostPerLiter(): LiveData<Double?>
    @Query("SELECT * FROM fuel_records ORDER BY date DESC LIMIT 1") fun getLastFuelRecord(): LiveData<FuelEntity?>
}
