package com.car.play.android.app.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DrivingScoreDao {
    @Insert suspend fun insert(score: DrivingScoreEntity)
    @Delete suspend fun delete(score: DrivingScoreEntity)
    @Query("SELECT * FROM driving_scores ORDER BY date DESC") fun getAllScores(): LiveData<List<DrivingScoreEntity>>
    @Query("SELECT AVG(overallScore) FROM driving_scores") fun getAverageScore(): LiveData<Double?>
    @Query("SELECT * FROM driving_scores ORDER BY date DESC LIMIT 1") fun getLatestScore(): LiveData<DrivingScoreEntity?>
    @Query("SELECT * FROM driving_scores ORDER BY overallScore DESC LIMIT 1") fun getBestScore(): LiveData<DrivingScoreEntity?>
}
