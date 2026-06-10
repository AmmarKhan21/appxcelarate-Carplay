package com.car.play.android.app.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FuelViewModel(application: Application) : AndroidViewModel(application) {

    private val fuelDao = AppDatabase.getDatabase(application).fuelDao()
    val allEntries: LiveData<List<FuelEntryEntity>> = fuelDao.getAll()

    fun addEntry(
        dateMillis: Long,
        odometer: Double,
        liters: Double,
        totalCost: Double,
        fullTank: Boolean
    ) {
        viewModelScope.launch {
            fuelDao.insert(
                FuelEntryEntity(
                    dateMillis = dateMillis,
                    odometer = odometer,
                    liters = liters,
                    totalCost = totalCost,
                    fullTank = fullTank
                )
            )
        }
    }

    fun deleteEntry(entry: FuelEntryEntity) {
        viewModelScope.launch { fuelDao.delete(entry) }
    }
}
