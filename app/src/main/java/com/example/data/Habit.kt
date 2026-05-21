package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val category: String, // "fitness", "mind", "learning", "routine"
    val metricName: String = "", // e.g. "pages", "miles", "minutes", or empty for check
    val metricTarget: Float = 0f, // target amount, e.g. 50.0, 0.9, 10.0, 0.0 for check
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
