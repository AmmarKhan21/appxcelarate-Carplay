package com.car.play.android.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_records")
data class FuelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val liters: Double,
    val costPerLiter: Double,
    val totalCost: Double,
    val odometer: Double,
    val fuelType: String,
    val station: String,
    val notes: String
)
