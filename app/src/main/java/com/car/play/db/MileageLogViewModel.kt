package com.car.play.android.app.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MileageLogViewModel(application: Application) : AndroidViewModel(application) {
    private val mileageDao = AppDatabase.getDatabase(application).mileageLogDao()
    val allLogs: LiveData<List<MileageLogEntity>> = mileageDao.getAllLogs()
    val totalBusinessMileage: LiveData<Double?> = mileageDao.getTotalBusinessMileage()
    val totalMileage: LiveData<Double?> = mileageDao.getTotalMileage()

    fun addLog(date: String, startOdometer: Double, endOdometer: Double, distance: Double, purpose: String, startLocation: String, endLocation: String, isBusinessTrip: Boolean, notes: String) {
        viewModelScope.launch {
            mileageDao.insert(MileageLogEntity(date = date, startOdometer = startOdometer, endOdometer = endOdometer, distance = distance, purpose = purpose, startLocation = startLocation, endLocation = endLocation, isBusinessTrip = isBusinessTrip, notes = notes))
        }
    }
    fun deleteLog(log: MileageLogEntity) { viewModelScope.launch { mileageDao.delete(log) } }
}
