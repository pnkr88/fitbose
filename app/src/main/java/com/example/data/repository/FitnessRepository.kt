package com.example.data.repository

import com.example.data.db.FitnessDao
import com.example.data.model.CalorieLog
import com.example.data.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

class FitnessRepository(private val fitnessDao: FitnessDao) {

    val allCalorieLogs: Flow<List<CalorieLog>> = fitnessDao.getAllCalorieLogs()
    val allWorkoutLogs: Flow<List<WorkoutLog>> = fitnessDao.getAllWorkoutLogs()

    fun getCalorieLogsInTimeframe(startTime: Long, endTime: Long): Flow<List<CalorieLog>> =
        fitnessDao.getCalorieLogsInTimeframe(startTime, endTime)

    fun getWorkoutLogsInTimeframe(startTime: Long, endTime: Long): Flow<List<WorkoutLog>> =
        fitnessDao.getWorkoutLogsInTimeframe(startTime, endTime)

    suspend fun insertCalorieLog(log: CalorieLog) {
        fitnessDao.insertCalorieLog(log)
    }

    suspend fun deleteCalorieLog(log: CalorieLog) {
        fitnessDao.deleteCalorieLog(log)
    }

    suspend fun deleteCalorieLogById(id: Int) {
        fitnessDao.deleteCalorieLogById(id)
    }

    suspend fun insertWorkoutLog(log: WorkoutLog) {
        fitnessDao.insertWorkoutLog(log)
    }

    suspend fun deleteWorkoutLog(log: WorkoutLog) {
        fitnessDao.deleteWorkoutLog(log)
    }

    suspend fun deleteWorkoutLogById(id: Int) {
        fitnessDao.deleteWorkoutLogById(id)
    }
}
