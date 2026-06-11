package com.car.play.android.app.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TripViewModel(application: Application) : AndroidViewModel(application) {
    private val tripDao = AppDatabase.getDatabase(application).tripDao()
    val allTrips: LiveData<List<TripEntity>> = tripDao.getAllTrips()
    val totalDistance: LiveData<Double?> = tripDao.getTotalDistance()
    val totalTrips: LiveData<Int?> = tripDao.getTotalTrips()
    val lastTrip: LiveData<TripEntity?> = tripDao.getLastTrip()

    fun addTrip(startTime: Long, endTime: Long, distance: Double, duration: Long, averageSpeed: Double, maxSpeed: Double, startAddress: String, endAddress: String, routePoints: String, date: String) {
        viewModelScope.launch {
            tripDao.insert(TripEntity(startTime = startTime, endTime = endTime, distance = distance, duration = duration, averageSpeed = averageSpeed, maxSpeed = maxSpeed, startAddress = startAddress, endAddress = endAddress, routePoints = routePoints, date = date))
        }
    }
    fun deleteTrip(trip: TripEntity) { viewModelScope.launch { tripDao.delete(trip) } }
}
