package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CalorieLog
import com.example.data.model.WorkoutLog
import com.example.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FitnessRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FitnessRepository(database.fitnessDao())
    }

    // Goal Configuration (Customizable by user)
    private val _calorieGoal = MutableStateFlow(2200)
    val calorieGoal: StateFlow<Int> = _calorieGoal.asStateFlow()

    private val _workoutBurnGoal = MutableStateFlow(400)
    val workoutBurnGoal: StateFlow<Int> = _workoutBurnGoal.asStateFlow()

    private val _proteinGoal = MutableStateFlow(140) // in grams
    val proteinGoal: StateFlow<Int> = _proteinGoal.asStateFlow()

    fun updateGoals(calories: Int, workoutsBurn: Int, protein: Int) {
        _calorieGoal.value = calories.coerceAtLeast(500)
        _workoutBurnGoal.value = workoutsBurn.coerceAtLeast(50)
        _proteinGoal.value = protein.coerceAtLeast(20)
    }

    // Flows of all data from DB
    val allCalorieLogs: StateFlow<List<CalorieLog>> = repository.allCalorieLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkoutLogs: StateFlow<List<WorkoutLog>> = repository.allWorkoutLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered lists for Today
    val todayCalorieLogs: StateFlow<List<CalorieLog>> = allCalorieLogs
        .combine(MutableStateFlow(System.currentTimeMillis())) { logs, _ ->
            val startAndEnd = getTodayStartAndEnd()
            logs.filter { it.timestamp in startAndEnd.first..startAndEnd.second }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayWorkoutLogs: StateFlow<List<WorkoutLog>> = allWorkoutLogs
        .combine(MutableStateFlow(System.currentTimeMillis())) { logs, _ ->
            val startAndEnd = getTodayStartAndEnd()
            logs.filter { it.timestamp in startAndEnd.first..startAndEnd.second }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary calculations for Today
    val todaySummary = combine(
        todayCalorieLogs,
        todayWorkoutLogs,
        calorieGoal,
        workoutBurnGoal,
        proteinGoal
    ) { calories, workouts, calGoal, burnGoal, protGoal ->
        val consumed = calories.sumOf { it.calories }
        val protein = calories.sumOf { it.proteinGrams }
        val carbs = calories.sumOf { it.carbsGrams }
        val fats = calories.sumOf { it.fatsGrams }
        
        val burned = workouts.sumOf { it.caloriesBurned }
        val activeMinutes = workouts.sumOf { it.durationMinutes }

        TodaySummaryState(
            caloriesConsumed = consumed,
            caloriesBurned = burned,
            activeMinutes = activeMinutes,
            totalProtein = protein,
            totalCarbs = carbs,
            totalFats = fats,
            calorieGoal = calGoal,
            workoutBurnGoal = burnGoal,
            proteinGoal = protGoal,
            netCalories = consumed - burned
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TodaySummaryState()
    )

    // DB Operations
    fun addCalorieLog(foodName: String, calories: Int, protein: Int, carbs: Int, fats: Int, mealType: String) {
        viewModelScope.launch {
            repository.insertCalorieLog(
                CalorieLog(
                    foodName = foodName,
                    calories = calories,
                    proteinGrams = protein,
                    carbsGrams = carbs,
                    fatsGrams = fats,
                    mealType = mealType
                )
            )
        }
    }

    fun deleteCalorieLog(log: CalorieLog) {
        viewModelScope.launch {
            repository.deleteCalorieLog(log)
        }
    }

    fun addWorkoutLog(workoutName: String, duration: Int, caloriesBurned: Int, workoutType: String, intensity: String) {
        viewModelScope.launch {
            repository.insertWorkoutLog(
                WorkoutLog(
                    workoutName = workoutName,
                    durationMinutes = duration,
                    caloriesBurned = caloriesBurned,
                    workoutType = workoutType,
                    intensity = intensity
                )
            )
        }
    }

    fun deleteWorkoutLog(log: WorkoutLog) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(log)
        }
    }

    // Get statistics for the last 7 days
    fun getWeeklyStats(): List<DailyStat> {
        val stats = mutableListOf<DailyStat>()
        val cal = Calendar.getInstance()
        
        // Let's gather values for the last 7 days (including today)
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            
            // Start of day
            dayCal.set(Calendar.HOUR_OF_DAY, 0)
            dayCal.set(Calendar.MINUTE, 0)
            dayCal.set(Calendar.SECOND, 0)
            dayCal.set(Calendar.MILLISECOND, 0)
            val start = dayCal.timeInMillis
            
            // End of day
            dayCal.set(Calendar.HOUR_OF_DAY, 23)
            dayCal.set(Calendar.MINUTE, 59)
            dayCal.set(Calendar.SECOND, 59)
            dayCal.set(Calendar.MILLISECOND, 999)
            val end = dayCal.timeInMillis

            val calLogs = allCalorieLogs.value.filter { it.timestamp in start..end }
            val workLogs = allWorkoutLogs.value.filter { it.timestamp in start..end }

            val dayOfWeekName = when (dayCal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "Sun"
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                else -> ""
            }

            stats.add(
                DailyStat(
                    dayName = dayOfWeekName,
                    caloriesConsumed = calLogs.sumOf { it.calories },
                    caloriesBurned = workLogs.sumOf { it.caloriesBurned },
                    timestamp = start
                )
            )
        }
        return stats
    }

    private fun getTodayStartAndEnd(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }
}

data class TodaySummaryState(
    val caloriesConsumed: Int = 0,
    val caloriesBurned: Int = 0,
    val activeMinutes: Int = 0,
    val totalProtein: Int = 0,
    val totalCarbs: Int = 0,
    val totalFats: Int = 0,
    val calorieGoal: Int = 2200,
    val workoutBurnGoal: Int = 400,
    val proteinGoal: Int = 140,
    val netCalories: Int = 0
)

data class DailyStat(
    val dayName: String,
    val caloriesConsumed: Int,
    val caloriesBurned: Int,
    val timestamp: Long
)
