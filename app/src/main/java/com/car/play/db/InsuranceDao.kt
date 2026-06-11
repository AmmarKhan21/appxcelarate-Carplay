package com.car.play.android.app.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface InsuranceDao {
    @Insert suspend fun insert(insurance: InsuranceEntity)
    @Update suspend fun update(insurance: InsuranceEntity)
    @Delete suspend fun delete(insurance: InsuranceEntity)
    @Query("SELECT * FROM insurance ORDER BY endDate DESC") fun getAllInsurance(): LiveData<List<InsuranceEntity>>
    @Query("SELECT * FROM insurance ORDER BY endDate ASC LIMIT 1") fun getNextExpiring(): LiveData<InsuranceEntity?>
}
