package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calorie_logs")
data class CalorieLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodName: String,
    val calories: Int,
    val proteinGrams: Int = 0,
    val carbsGrams: Int = 0,
    val fatsGrams: Int = 0,
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val timestamp: Long = System.currentTimeMillis()
)
