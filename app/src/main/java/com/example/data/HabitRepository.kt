package com.example.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allHistory: Flow<List<HabitHistory>> = habitDao.getAllHistory()

    fun getHistoryForDate(dateString: String): Flow<List<HabitHistory>> {
        return habitDao.getHistoryForDate(dateString)
    }

    fun getHistoryForHabit(habitId: Int): Flow<List<HabitHistory>> {
        return habitDao.getHistoryForHabit(habitId)
    }

    suspend fun getHabitById(id: Int): Habit? {
        return habitDao.getHabitById(id)
    }

    suspend fun insertHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun insertHistoryList(historyList: List<HabitHistory>) {
        habitDao.insertHistoryList(historyList)
    }

    suspend fun clearAllData() {
        habitDao.deleteAllHistory()
        habitDao.deleteAllHabits()
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
        habitDao.deleteAllHistoryForHabit(habit.id)
    }

    suspend fun toggleHabitCompletion(habitId: Int, date: LocalDate, metricValue: Float, updateStreak: Boolean = true): Boolean {
        val dateString = date.toString() // "YYYY-MM-DD"
        val existingHistory = habitDao.getHistoryForHabitAndDateSync(habitId, dateString)

        val isNowCompleted: Boolean
        if (existingHistory != null) {
            // Already toggled, so remove or check if metric is updated. Or, if it's a binary check, toggle it off
            val habit = habitDao.getHabitById(habitId) ?: return false
            if (habit.metricName.isEmpty()) {
                // Yes/No check: toggle off
                habitDao.deleteHistoryForHabitAndDate(habitId, dateString)
                isNowCompleted = false
            } else {
                // Metric check: if values match, toggle off, otherwise update value
                if (existingHistory.metricValue == metricValue) {
                    habitDao.deleteHistoryForHabitAndDate(habitId, dateString)
                    isNowCompleted = false
                } else {
                    habitDao.updateHistory(existingHistory.copy(metricValue = metricValue))
                    isNowCompleted = metricValue >= habit.metricTarget
                }
            }
        } else {
            // Not completed, create new history
            habitDao.insertHistory(
                HabitHistory(
                    habitId = habitId,
                    dateString = dateString,
                    metricValue = metricValue
                )
            )
            val habit = habitDao.getHabitById(habitId) ?: return false
            isNowCompleted = habit.metricName.isEmpty() || metricValue >= habit.metricTarget
        }

        // Recalculate streak for this habit
        if (updateStreak) {
            recalculateStreak(habitId)
        }
        return isNowCompleted
    }

    suspend fun updateHabitMetric(habitId: Int, date: LocalDate, value: Float) {
        val dateString = date.toString()
        val habit = habitDao.getHabitById(habitId) ?: return
        val existing = habitDao.getHistoryForHabitAndDateSync(habitId, dateString)

        if (value > 0f) {
            if (existing != null) {
                habitDao.updateHistory(existing.copy(metricValue = value))
            } else {
                habitDao.insertHistory(
                    HabitHistory(
                        habitId = habitId,
                        dateString = dateString,
                        metricValue = value
                    )
                )
            }
        } else {
            if (existing != null) {
                habitDao.deleteHistoryForHabitAndDate(habitId, dateString)
            }
        }

        recalculateStreak(habitId)
    }

    // Dynamic, historical back-scan to accurately compute current streak and best streak
    suspend fun recalculateStreak(habitId: Int) {
        val habit = habitDao.getHabitById(habitId) ?: return
        val historyList = habitDao.getHistoryForHabitSync(habitId)

        val completedDates = historyList.filter {
            habit.metricName.isEmpty() || it.metricValue >= habit.metricTarget
        }.map { it.dateString }.toSet()

        val today = LocalDate.now()
        var currentStreak = 0

        // Find current streak by scanning backwards from today (if completed today) or yesterday
        val completedToday = completedDates.contains(today.toString())
        var checkDate = if (completedToday) today else today.minusDays(1)

        while (completedDates.contains(checkDate.toString())) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        // Find best streak historically by scanning the last 180 days in order
        var bestStreak = habit.bestStreak
        var tempStreak = 0
        var trackingBest = 0
        var scanDate = today.minusDays(180)
        while (!scanDate.isAfter(today)) {
            if (completedDates.contains(scanDate.toString())) {
                tempStreak++
                if (tempStreak > trackingBest) {
                    trackingBest = tempStreak
                }
            } else {
                tempStreak = 0
            }
            scanDate = scanDate.plusDays(1)
        }

        bestStreak = maxOf(bestStreak, trackingBest, currentStreak)

        habitDao.updateHabit(
            habit.copy(
                streak = currentStreak,
                bestStreak = bestStreak
            )
        )
    }

    // Helper to generate realistic historical data for visual impact
    suspend fun prepopulateHistoricalDataIfEmpty() {
        val today = LocalDate.now()
        // Check if DB already has habits
        val currentList = habitDao.getHistoryForDateSync(today.toString())
        // Since Flow can be empty, let's sync check
        // If we want to check if any habit exists, let's count them
        // Let's insert a couple of amazing habits if empty
    }
}
