package com.car.play.android.app.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CarProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val carProfileDao = AppDatabase.getDatabase(application).carProfileDao()
    val allCars: LiveData<List<CarProfileEntity>> = carProfileDao.getAllCars()
    val activeCar: LiveData<CarProfileEntity?> = carProfileDao.getActiveCar()

    fun addCar(name: String, make: String, model: String, year: String, color: String, licensePlate: String, vin: String, purchaseDate: String, imagePath: String) {
        viewModelScope.launch {
            carProfileDao.insert(CarProfileEntity(name = name, make = make, model = model, year = year, color = color, licensePlate = licensePlate, vin = vin, purchaseDate = purchaseDate, imagePath = imagePath, isActive = false))
        }
    }
    fun deleteCar(car: CarProfileEntity) { viewModelScope.launch { carProfileDao.delete(car) } }
    fun setActive(id: Long) { viewModelScope.launch { carProfileDao.deactivateAll(); carProfileDao.setActiveCar(id) } }
}
