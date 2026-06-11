package com.car.play.android.app.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DrivingScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val scoreDao = AppDatabase.getDatabase(application).drivingScoreDao()
    val allScores: LiveData<List<DrivingScoreEntity>> = scoreDao.getAllScores()
    val averageScore: LiveData<Double?> = scoreDao.getAverageScore()
    val latestScore: LiveData<DrivingScoreEntity?> = scoreDao.getLatestScore()
    val bestScore: LiveData<DrivingScoreEntity?> = scoreDao.getBestScore()

    fun addScore(date: String, overallScore: Int, accelerationScore: Int, brakingScore: Int, speedScore: Int, corneringScore: Int, distanceDriven: Double, duration: Long, hardBrakes: Int, rapidAccelerations: Int) {
        viewModelScope.launch {
            scoreDao.insert(DrivingScoreEntity(date = date, overallScore = overallScore, accelerationScore = accelerationScore, brakingScore = brakingScore, speedScore = speedScore, corneringScore = corneringScore, distanceDriven = distanceDriven, duration = duration, hardBrakes = hardBrakes, rapidAccelerations = rapidAccelerations))
        }
    }
    fun deleteScore(score: DrivingScoreEntity) { viewModelScope.launch { scoreDao.delete(score) } }
}
