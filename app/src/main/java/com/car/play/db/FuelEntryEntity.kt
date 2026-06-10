package com.car.play.android.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_entry")
data class FuelEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val odometer: Double,
    val liters: Double,
    val totalCost: Double,
    val fullTank: Boolean
)
