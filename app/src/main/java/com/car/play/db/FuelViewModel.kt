package com.car.play.android.app.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FuelViewModel(application: Application) : AndroidViewModel(application) {
    private val fuelDao = AppDatabase.getDatabase(application).fuelDao()
    val allFuelRecords: LiveData<List<FuelEntity>> = fuelDao.getAllFuelRecords()
    val totalFuelCost: LiveData<Double?> = fuelDao.getTotalFuelCost()
    val averageCostPerLiter: LiveData<Double?> = fuelDao.getAverageCostPerLiter()
    val lastFuelRecord: LiveData<FuelEntity?> = fuelDao.getLastFuelRecord()

    fun addFuelRecord(date: String, liters: Double, costPerLiter: Double, totalCost: Double, odometer: Double, fuelType: String, station: String, notes: String) {
        viewModelScope.launch {
            fuelDao.insert(FuelEntity(date = date, liters = liters, costPerLiter = costPerLiter, totalCost = totalCost, odometer = odometer, fuelType = fuelType, station = station, notes = notes))
        }
    }
    fun deleteFuelRecord(fuel: FuelEntity) { viewModelScope.launch { fuelDao.delete(fuel) } }
}
