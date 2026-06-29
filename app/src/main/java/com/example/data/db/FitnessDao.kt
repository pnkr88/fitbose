package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CalorieLog
import com.example.data.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {

    // --- Calorie Logs ---
    @Query("SELECT * FROM calorie_logs ORDER BY timestamp DESC")
    fun getAllCalorieLogs(): Flow<List<CalorieLog>>

    @Query("SELECT * FROM calorie_logs WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getCalorieLogsInTimeframe(startTime: Long, endTime: Long): Flow<List<CalorieLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalorieLog(log: CalorieLog)

    @Delete
    suspend fun deleteCalorieLog(log: CalorieLog)

    @Query("DELETE FROM calorie_logs WHERE id = :id")
    suspend fun deleteCalorieLogById(id: Int)


    // --- Workout Logs ---
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getWorkoutLogsInTimeframe(startTime: Long, endTime: Long): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLog)

    @Delete
    suspend fun deleteWorkoutLog(log: WorkoutLog)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteWorkoutLogById(id: Int)
}
