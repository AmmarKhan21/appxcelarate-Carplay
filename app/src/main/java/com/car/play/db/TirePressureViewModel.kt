package com.car.play.android.app.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TirePressureViewModel(application: Application) : AndroidViewModel(application) {
    private val tireDao = AppDatabase.getDatabase(application).tirePressureDao()
    val allRecords: LiveData<List<TirePressureEntity>> = tireDao.getAllRecords()
    val latestRecord: LiveData<TirePressureEntity?> = tireDao.getLatestRecord()

    fun addRecord(frontLeft: Double, frontRight: Double, rearLeft: Double, rearRight: Double, recommendedPressure: Double, date: String, notes: String) {
        viewModelScope.launch {
            tireDao.insert(TirePressureEntity(frontLeft = frontLeft, frontRight = frontRight, rearLeft = rearLeft, rearRight = rearRight, recommendedPressure = recommendedPressure, date = date, notes = notes))
        }
    }
    fun deleteRecord(record: TirePressureEntity) { viewModelScope.launch { tireDao.delete(record) } }
}
