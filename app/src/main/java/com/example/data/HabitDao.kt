package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Int): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habit_history ORDER BY dateString ASC")
    fun getAllHistory(): Flow<List<HabitHistory>>

    @Query("SELECT * FROM habit_history WHERE dateString = :dateString")
    fun getHistoryForDate(dateString: String): Flow<List<HabitHistory>>

    @Query("SELECT * FROM habit_history WHERE habitId = :habitId ORDER BY dateString DESC")
    fun getHistoryForHabit(habitId: Int): Flow<List<HabitHistory>>

    @Query("SELECT * FROM habit_history WHERE habitId = :habitId ORDER BY dateString DESC")
    suspend fun getHistoryForHabitSync(habitId: Int): List<HabitHistory>

    @Query("SELECT * FROM habit_history WHERE dateString = :dateString")
    suspend fun getHistoryForDateSync(dateString: String): List<HabitHistory>

    @Query("SELECT * FROM habit_history WHERE habitId = :habitId AND dateString = :date")
    suspend fun getHistoryForHabitAndDateSync(habitId: Int, date: String): HabitHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HabitHistory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryList(historyList: List<HabitHistory>)

    @Update
    suspend fun updateHistory(history: HabitHistory)

    @Query("DELETE FROM habit_history")
    suspend fun deleteAllHistory()

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query("DELETE FROM habit_history WHERE habitId = :habitId AND dateString = :date")
    suspend fun deleteHistoryForHabitAndDate(habitId: Int, date: String)

    @Query("DELETE FROM habit_history WHERE habitId = :habitId")
    suspend fun deleteAllHistoryForHabit(habitId: Int)
}
