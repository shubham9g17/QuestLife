package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_history")
data class HabitHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val dateString: String, // "YYYY-MM-DD"
    val metricValue: Float, // completed amount (e.g. 50.0, or 1.0 for checked, 0.0 for unchecked)
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
