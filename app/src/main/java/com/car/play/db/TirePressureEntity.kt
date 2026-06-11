package com.car.play.android.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tire_pressure")
data class TirePressureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val frontLeft: Double,
    val frontRight: Double,
    val rearLeft: Double,
    val rearRight: Double,
    val recommendedPressure: Double,
    val date: String,
    val notes: String
)
