package com.car.play.android.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "driving_scores")
data class DrivingScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val overallScore: Int,
    val accelerationScore: Int,
    val brakingScore: Int,
    val speedScore: Int,
    val corneringScore: Int,
    val distanceDriven: Double,
    val duration: Long,
    val hardBrakes: Int,
    val rapidAccelerations: Int
)
