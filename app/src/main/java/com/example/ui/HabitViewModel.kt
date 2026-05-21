package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository
    
    val habits: StateFlow<List<Habit>>
    val allHistory: StateFlow<List<HabitHistory>>

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentTab = MutableStateFlow("board") // "board", "guild", "mascot"
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _activeFilter = MutableStateFlow("ALL") // "ALL", "fitness", "mind", "learning", "routine"
    val activeFilter: StateFlow<String> = _activeFilter.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _isUserOnboarded = MutableStateFlow(false)
    val isUserOnboarded: StateFlow<Boolean> = _isUserOnboarded.asStateFlow()

    private val _userPersona = MutableStateFlow("Adventurer")
    val userPersona: StateFlow<String> = _userPersona.asStateFlow()

    private val _userColor = MutableStateFlow("Cyan")
    val userColor: StateFlow<String> = _userColor.asStateFlow()

    init {
        val habitDao = AppDatabase.getDatabase(application).habitDao()
        repository = HabitRepository(habitDao)

        val prefs = application.getSharedPreferences("questlife_prefs", android.content.Context.MODE_PRIVATE)
        _userEmail.value = prefs.getString("user_email", null)
        _isUserOnboarded.value = prefs.getBoolean("is_onboarded", false)
        _userPersona.value = prefs.getString("user_persona", "Adventurer") ?: "Adventurer"
        _userColor.value = prefs.getString("user_color", "Cyan") ?: "Cyan"

        habits = repository.allHabits
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        allHistory = repository.allHistory
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // No auto-seeding. Seeding is requested explicitly upon Onboarding or custom actions
    }

    fun onboardUser(email: String, persona: String, color: String, isNewUser: Boolean) {
        val prefs = getApplication<Application>().getSharedPreferences("questlife_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("user_email", email)
            putBoolean("is_onboarded", true)
            putString("user_persona", persona)
            putString("user_color", color)
            apply()
        }
        _userEmail.value = email
        _isUserOnboarded.value = true
        _userPersona.value = persona
        _userColor.value = color

        viewModelScope.launch {
            if (isNewUser) {
                // Clear all existing data so that a brand new user doesn't start with someone else's or default habits
                repository.clearAllData()
            } else {
                // Seed preloaded mock records for a beautiful dashboard experience
                repository.clearAllData()
                seedData()
            }
        }
    }

    fun logout() {
        val prefs = getApplication<Application>().getSharedPreferences("questlife_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        _userEmail.value = null
        _isUserOnboarded.value = false
        _userPersona.value = "Adventurer"
        _userColor.value = "Cyan"
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setCurrentTab(tab: String) {
        _currentTab.value = tab
    }

    fun setActiveFilter(filter: String) {
        _activeFilter.value = filter
    }

    // Toggle habit state
    fun toggleHabit(habitId: Int, metricValue: Float = 1f) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, _selectedDate.value, metricValue)
        }
    }

    // Update custom slider or field values
    fun updateHabitValue(habitId: Int, value: Float) {
        viewModelScope.launch {
            repository.updateHabitMetric(habitId, _selectedDate.value, value)
        }
    }

    fun addHabit(
        name: String,
        description: String,
        category: String,
        metricName: String = "",
        metricTarget: Float = 0f
    ) {
        viewModelScope.launch {
            repository.insertHabit(
                Habit(
                    name = name,
                    description = description,
                    category = category,
                    metricName = metricName,
                    metricTarget = metricTarget
                )
            )
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Generate rich gamified historical completions
    private suspend fun seedData() {
        val db = AppDatabase.getDatabase(getApplication())
        db.withTransaction {
            val defaultHabits = listOf(
                Habit(
                    name = "Wake up early",
                    description = "Early bird gets the worm! Woke up before 7 AM?",
                    category = "routine",
                    metricName = "",
                    metricTarget = 0f,
                    streak = 5,
                    bestStreak = 12
                ),
                Habit(
                    name = "Cook healthy dinner",
                    description = "Whip up a chef masterpiece. No fast food today!",
                    category = "routine",
                    metricName = "",
                    metricTarget = 0f,
                    streak = 3,
                    bestStreak = 8
                ),
                Habit(
                    name = "Write journal",
                    description = "Pour your thoughts. What made you smile today?",
                    category = "mind",
                    metricName = "",
                    metricTarget = 0f,
                    streak = 4,
                    bestStreak = 15
                ),
                Habit(
                    name = "Track work hours",
                    description = "Master your hours. Logged your productive focus?",
                    category = "routine",
                    metricName = "",
                    metricTarget = 0f,
                    streak = 8,
                    bestStreak = 24
                ),
                Habit(
                    name = "Zen Meditation",
                    description = "Ready for a breath of fresh air? Let's get those 10 minutes.",
                    category = "mind",
                    metricName = "min",
                    metricTarget = 10f,
                    streak = 12,
                    bestStreak = 20
                ),
                Habit(
                    name = "Run Outdoors",
                    description = "Fast and furious! Conquer your target mileage today.",
                    category = "fitness",
                    metricName = "miles",
                    metricTarget = 2.0f,
                    streak = 2,
                    bestStreak = 6
                ),
                Habit(
                    name = "Read Books",
                    description = "Dive into another world. How many pages did you turn?",
                    category = "learning",
                    metricName = "pages",
                    metricTarget = 30f,
                    streak = 6,
                    bestStreak = 16
                ),
                Habit(
                    name = "Speak French",
                    description = "Bonjour! Practice Duolingo to build your quest levels.",
                    category = "learning",
                    metricName = "",
                    metricTarget = 0f,
                    streak = 15,
                    bestStreak = 30
                )
            )

            // Seed habits and fetch IDs
            val insertedIds = mutableListOf<Int>()
            for (habit in defaultHabits) {
                val id = repository.insertHabit(habit).toInt()
                insertedIds.add(id)
            }

            // Prepopulate past 105 days (15 weeks) with realistic compliance rates
            val today = LocalDate.now()
            var seedCounter = 0
            val historyList = mutableListOf<HabitHistory>()

            for (dayOffset in 1..105) {
                val pastDate = today.minusDays(dayOffset.toLong())
                val dateStr = pastDate.toString()
                
                // Generate deterministic but high-utility looking history
                for (index in defaultHabits.indices) {
                    val habitId = insertedIds[index]
                    val habit = defaultHabits[index]
                    
                    // Set custom completion percentages depending on habit
                    val completeChance = when (index) {
                        0 -> 72 // Wake early
                        1 -> 58 // Dinner
                        2 -> 65 // Journal
                        3 -> 85 // Track time
                        4 -> 80 // Meditate (Metric)
                        5 -> 40 // Run (Metric, fitness is intermittent)
                        6 -> 68 // Read (Metric)
                        7 -> 90 // Speak French (Duolingo effect!)
                        else -> 50
                    }

                    // Simple pseudo-random logic that depends on habit and day so it stays static per build
                    seedCounter = (seedCounter * 31 + dayOffset * 17 + index * 997) % 100
                    val randomVal = Math.abs(seedCounter)

                    if (randomVal < completeChance) {
                        val metricValue = when {
                            habit.metricName == "min" -> 10f + (randomVal % 15) // 10 to 24 mins
                            habit.metricName == "miles" -> 2.0f + ((randomVal % 10) / 5f) // 2.0 to 3.8 miles
                            habit.metricName == "pages" -> 30f + (randomVal % 40) // 30 to 69 pages
                            else -> 1f // Checkbox
                        }
                        
                        historyList.add(
                            HabitHistory(
                                habitId = habitId,
                                dateString = dateStr,
                                metricValue = metricValue
                            )
                        )
                    }
                }
            }

            // Insert all history logs in a single batch
            repository.insertHistoryList(historyList)

            // Recalculate streaks for all seeded habits
            for (id in insertedIds) {
                repository.recalculateStreak(id)
            }
        }
    }
}
