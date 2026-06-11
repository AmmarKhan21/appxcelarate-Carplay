package com.car.play.android.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mileage_logs")
data class MileageLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val startOdometer: Double,
    val endOdometer: Double,
    val distance: Double,
    val purpose: String,
    val startLocation: String,
    val endLocation: String,
    val isBusinessTrip: Boolean,
    val notes: String
)
