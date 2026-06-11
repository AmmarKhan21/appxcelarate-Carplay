package com.car.play.android.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "car_profiles")
data class CarProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val make: String,
    val model: String,
    val year: String,
    val color: String,
    val licensePlate: String,
    val vin: String,
    val purchaseDate: String,
    val imagePath: String,
    val isActive: Boolean
)
