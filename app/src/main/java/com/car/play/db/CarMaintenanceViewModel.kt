package com.car.play.android.app.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CarMaintenanceViewModel(application: Application) : AndroidViewModel(application) {

    private val carMaintenanceDao = AppDatabase.getDatabase(application).carMaintenanceDao()
    val allServices: LiveData<List<CarMaintenanceEntity>> = carMaintenanceDao.getAllCarMaintenance()

    fun addService(serviceName: String, cost: String ,date:String) {
        val service = CarMaintenanceEntity(serviceName = serviceName, cost = cost, date = date)
        viewModelScope.launch {
            carMaintenanceDao.insert(service)
        }
    }
    fun deleteService(service: CarMaintenanceEntity) {
        viewModelScope.launch {
            carMaintenanceDao.delete(service)
        }
    }
}
