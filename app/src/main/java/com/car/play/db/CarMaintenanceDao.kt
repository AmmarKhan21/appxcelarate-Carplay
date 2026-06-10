package com.car.play.android.app.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CarMaintenanceDao {

    @Insert
    suspend fun insert(carMaintenance: CarMaintenanceEntity)

    @Query("SELECT * FROM car_maintenance")
    fun getAllCarMaintenance(): LiveData<List<CarMaintenanceEntity>>

    @Delete
    suspend fun delete(carMaintenance: CarMaintenanceEntity)
}
