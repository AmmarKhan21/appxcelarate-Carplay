package com.car.play.android.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val distance: Double,
    val duration: Long,
    val averageSpeed: Double,
    val maxSpeed: Double,
    val startAddress: String,
    val endAddress: String,
    val routePoints: String,
    val date: String
)
