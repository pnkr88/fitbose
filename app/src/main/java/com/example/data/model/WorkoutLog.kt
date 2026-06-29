package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workoutName: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val workoutType: String, // "Cardio", "Strength", "HIIT", "Flexibility", "Other"
    val intensity: String, // "Low", "Medium", "High"
    val timestamp: Long = System.currentTimeMillis()
)
