package com.car.play.android.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insurance")
data class InsuranceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val policyNumber: String,
    val provider: String,
    val type: String,
    val premium: Double,
    val startDate: String,
    val endDate: String,
    val agentName: String,
    val agentPhone: String,
    val documentPath: String,
    val notes: String
)
