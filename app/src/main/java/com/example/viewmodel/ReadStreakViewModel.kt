package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AuthResult
import com.example.data.Book
import com.example.data.ReadingLog
import com.example.data.ReadingRepository
import com.example.data.ReadingSession
import com.example.data.UserEntity
import com.example.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.ui.components.GoalStreakCalculation
import com.example.ui.components.StreakCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReadStreakViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    }

    private val repository: ReadingRepository
    private val userRepository: UserRepository
    private val prefs = application.getSharedPreferences("read_streak_app_prefs", Context.MODE_PRIVATE)

    val allBooks: StateFlow<List<Book>>
    val allLogs: StateFlow<List<ReadingLog>>
    val allSessions: StateFlow<List<ReadingSession>>

    // Local Auth State
    val currentUser: StateFlow<UserEntity?>

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // UI state parameters
    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _bestStreak = MutableStateFlow(0)
    val bestStreak: StateFlow<Int> = _bestStreak.asStateFlow()

    private val _totalDaysRead = MutableStateFlow(0)
    val totalDaysRead: StateFlow<Int> = _totalDaysRead.asStateFlow()

    private val _completedDaysGoal = MutableStateFlow(0)
    val completedDaysGoal: StateFlow<Int> = _completedDaysGoal.asStateFlow()

    private val _dailyGoalMinutes = MutableStateFlow(prefs.getInt("daily_goal_minutes", 20))
    val dailyGoalMinutes: StateFlow<Int> = _dailyGoalMinutes.asStateFlow()

    private val _dailyGoalPages = MutableStateFlow(prefs.getInt("daily_goal_pages", 15))
    val dailyGoalPages: StateFlow<Int> = _dailyGoalPages.asStateFlow()

    private val _dailyGoalType = MutableStateFlow(prefs.getString("daily_goal_type", "MINUTES") ?: "MINUTES")
    val dailyGoalType: StateFlow<String> = _dailyGoalType.asStateFlow()

    val yearlyGoal: Int = 365

    // Mon, Tue, Wed, Thu, Fri, Sat, Sun
    private val _weekDaysRead = MutableStateFlow<List<Boolean>>(List(7) { false })
    val weekDaysRead: StateFlow<List<Boolean>> = _weekDaysRead.asStateFlow()

    private val _isTodayRead = MutableStateFlow(false)
    val isTodayRead: StateFlow<Boolean> = _isTodayRead.asStateFlow()

    private val _showSuccessModal = MutableStateFlow(false)
    val showSuccessModal: StateFlow<Boolean> = _showSuccessModal.asStateFlow()

    private val _showGoProPaywall = MutableStateFlow(false)
    val showGoProPaywall: StateFlow<Boolean> = _showGoProPaywall.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(prefs.getBoolean("has_completed_onboarding", false))
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "Light") ?: "Light") // Light, Dark, System
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(prefs.getBoolean("haptic_feedback", true))
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()

    private val _dailyPrompt = MutableStateFlow(prefs.getBoolean("daily_prompt_enabled", true))
    val dailyPrompt: StateFlow<Boolean> = _dailyPrompt.asStateFlow()

    private val _reminderTime = MutableStateFlow(prefs.getString("reminder_time", "8:00 PM") ?: "8:00 PM")
    val reminderTime: StateFlow<String> = _reminderTime.asStateFlow()

    private val _isProUnlocked = MutableStateFlow(true)
    val isProUnlocked: StateFlow<Boolean> = _isProUnlocked.asStateFlow()

    // Gamification & Quest State (Level, XP, Gems, Boost)
    private val _userLevel = MutableStateFlow(prefs.getInt("user_level", 7))
    val userLevel: StateFlow<Int> = _userLevel.asStateFlow()

    private val _currentXp = MutableStateFlow(prefs.getInt("current_xp", 1525))
    val currentXp: StateFlow<Int> = _currentXp.asStateFlow()

    val xpForNextLevel: Int = 2000

    private val _gemsCount = MutableStateFlow(prefs.getInt("gems_count", 340))
    val gemsCount: StateFlow<Int> = _gemsCount.asStateFlow()

    private val _isBoostActive = MutableStateFlow(prefs.getBoolean("is_boost_active", true))
    val isBoostActive: StateFlow<Boolean> = _isBoostActive.asStateFlow()

    val boostMultiplier: String = "1.5x"

    private val _todayMinutesRead = MutableStateFlow(0)
    val todayMinutesRead: StateFlow<Int> = _todayMinutesRead.asStateFlow()

    // Session Confirmation Modal Details
    private val _lastLoggedBookTitle = MutableStateFlow("Atomic Habits")
    val lastLoggedBookTitle: StateFlow<String> = _lastLoggedBookTitle.asStateFlow()

    private val _lastLoggedMinutes = MutableStateFlow(25)
    val lastLoggedMinutes: StateFlow<Int> = _lastLoggedMinutes.asStateFlow()

    private val _lastLoggedPages = MutableStateFlow(18)
    val lastLoggedPages: StateFlow<Int> = _lastLoggedPages.asStateFlow()

    private val _lastBaseXp = MutableStateFlow(50)
    val lastBaseXp: StateFlow<Int> = _lastBaseXp.asStateFlow()

    private val _lastXpGained = MutableStateFlow(75)
    val lastXpGained: StateFlow<Int> = _lastXpGained.asStateFlow()

    private val _lastGemsGained = MutableStateFlow(5)
    val lastGemsGained: StateFlow<Int> = _lastGemsGained.asStateFlow()

    private val todayDateKey = LocalDate.now().format(DATE_FORMATTER)
    private val _isDailyQuestClaimed = MutableStateFlow(prefs.getBoolean("quest_claimed_$todayDateKey", false))
    val isDailyQuestClaimed: StateFlow<Boolean> = _isDailyQuestClaimed.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReadingRepository(database.readingDao(), database.readingSessionDao())
        userRepository = UserRepository(database.userDao())

        allBooks = repository.allBooks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allLogs = repository.allLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allSessions = repository.allSessions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        currentUser = userRepository.loggedInUser
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        // Compute consecutive goal-met streak stats on a background thread and update UI state
        viewModelScope.launch {
            combine(repository.allLogs, _dailyGoalMinutes) { logs, goalMins ->
                computeStreakAndWeekStatus(logs, goalMins)
            }
                .flowOn(Dispatchers.Default)
                .collect { (calculation, weekStatus) ->
                    _currentStreak.value = calculation.currentStreak
                    _bestStreak.value = calculation.bestStreak
                    _totalDaysRead.value = calculation.totalActiveDays
                    _completedDaysGoal.value = calculation.totalGoalMetDays
                    _isTodayRead.value = calculation.isTodayGoalMet
                    _todayMinutesRead.value = calculation.todayMinutesRead
                    _weekDaysRead.value = weekStatus
                }
        }
    }

    fun userTitleForLevel(level: Int): String {
        return when {
            level >= 15 -> "Eternal Flame"
            level >= 10 -> "Master of Tomes"
            level >= 7 -> "Flamekeeper"
            level >= 5 -> "Flame Initiate"
            level >= 3 -> "Page Turner"
            else -> "Novice Reader"
        }
    }

    private fun computeStreakAndWeekStatus(
        logs: List<ReadingLog>,
        goalMinutes: Int = _dailyGoalMinutes.value
    ): Pair<GoalStreakCalculation, List<Boolean>> {
        val calculation = StreakCalculator.calculateGoalStreak(
            logs = logs,
            dailyGoalMins = goalMinutes
        )

        // Calculate Week Days (Mon-Sun) where goal was met
        val today = LocalDate.now()
        val effectiveGoal = goalMinutes.coerceAtLeast(1)
        val minutesByDate = HashMap<LocalDate, Int>(logs.size)
        for (log in logs) {
            try {
                val parsed = LocalDate.parse(log.dateString, DATE_FORMATTER)
                minutesByDate[parsed] = (minutesByDate[parsed] ?: 0) + log.minutesRead
            } catch (_: Exception) {}
        }

        val dayOfWeek = today.dayOfWeek.value // 1 = Mon, 7 = Sun
        val monday = today.minusDays((dayOfWeek - 1).toLong())
        val weekStatus = (0..6).map { i ->
            val day = monday.plusDays(i.toLong())
            (minutesByDate[day] ?: 0) >= effectiveGoal
        }
        return calculation to weekStatus
    }

    fun addXpAndGems(xpAmount: Int, gemsAmount: Int) {
        val effectiveXp = if (_isBoostActive.value) (xpAmount * 1.5).toInt() else xpAmount
        _lastBaseXp.value = xpAmount
        _lastXpGained.value = effectiveXp
        _lastGemsGained.value = gemsAmount

        var newXp = _currentXp.value + effectiveXp
        var newLevel = _userLevel.value
        while (newXp >= xpForNextLevel) {
            newXp -= xpForNextLevel
            newLevel += 1
        }
        _currentXp.value = newXp
        _userLevel.value = newLevel
        val newGems = _gemsCount.value + gemsAmount
        _gemsCount.value = newGems

        prefs.edit()
            .putInt("current_xp", newXp)
            .putInt("user_level", newLevel)
            .putInt("gems_count", newGems)
            .apply()
    }

    fun claimDailyQuest() {
        if (_isDailyQuestClaimed.value) return
        addXpAndGems(50, 10)
        _isDailyQuestClaimed.value = true
        prefs.edit().putBoolean("quest_claimed_$todayDateKey", true).apply()
    }

    fun logReadingSession(
        bookTitle: String,
        minutesRead: Int,
        pagesRead: Int,
        notes: String = ""
    ) {
        val todayStr = LocalDate.now().format(DATE_FORMATTER)
        val effectiveBook = bookTitle.trim().ifBlank {
            allBooks.value.firstOrNull { !it.isCompleted }?.title ?: "Atomic Habits"
        }
        val loggedMins = minutesRead.coerceAtLeast(1)
        val loggedPgs = pagesRead.coerceAtLeast(1)
        val baseXp = (loggedMins * 2).coerceIn(20, 200)
        val effectiveXp = if (_isBoostActive.value) (baseXp * 1.5).toInt() else baseXp

        _lastLoggedBookTitle.value = effectiveBook
        _lastLoggedMinutes.value = loggedMins
        _lastLoggedPages.value = loggedPgs

        viewModelScope.launch {
            repository.insertSession(
                ReadingSession(
                    bookTitle = effectiveBook,
                    minutesRead = loggedMins,
                    pagesRead = loggedPgs,
                    dateString = todayStr,
                    notes = notes.trim(),
                    xpEarned = effectiveXp
                )
            )
            addXpAndGems(baseXp, 5)
        }
        _showSuccessModal.value = true
    }

    fun deleteReadingSession(sessionId: Int) {
        viewModelScope.launch {
            repository.deleteSessionById(sessionId)
        }
    }

    fun logTodayReading(minutes: Int = maxOf(25, _dailyGoalMinutes.value)) {
        val loggedMins = maxOf(minutes, _dailyGoalMinutes.value)
        val loggedPages = ((loggedMins * 18) / 25).coerceAtLeast(10)
        val activeBookTitle = allBooks.value.firstOrNull { !it.isCompleted }?.title ?: "Atomic Habits"
        logReadingSession(
            bookTitle = activeBookTitle,
            minutesRead = loggedMins,
            pagesRead = loggedPages
        )
    }

    fun logCompletedTimerSession(
        minutes: Int,
        bookTitle: String,
        pagesRead: Int = ((minutes.coerceAtLeast(1) * 18) / 25).coerceAtLeast(5)
    ) {
        val loggedMins = minutes.coerceAtLeast(1)
        val loggedPages = pagesRead.coerceAtLeast(1)
        logReadingSession(
            bookTitle = bookTitle.ifBlank {
                allBooks.value.firstOrNull { !it.isCompleted }?.title ?: "Atomic Habits"
            },
            minutesRead = loggedMins,
            pagesRead = loggedPages
        )
    }

    fun saveSessionNote(note: String) {
        prefs.edit().putString("session_note_$todayDateKey", note).apply()
        viewModelScope.launch {
            repository.updateLatestSessionNote(note)
        }
    }

    fun dismissSuccessModal() {
        _showSuccessModal.value = false
    }

    fun openGoProPaywall() {
        _showGoProPaywall.value = false
    }

    fun dismissGoProPaywall() {
        _showGoProPaywall.value = false
    }

    fun unlockPro() {
        _isProUnlocked.value = true
        _showGoProPaywall.value = false
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun setHapticFeedback(enabled: Boolean) {
        _hapticFeedback.value = enabled
        prefs.edit().putBoolean("haptic_feedback", enabled).apply()
    }

    fun setDailyPrompt(context: android.content.Context, enabled: Boolean) {
        _dailyPrompt.value = enabled
        prefs.edit().putBoolean("daily_prompt_enabled", enabled).apply()
        if (enabled) {
            com.example.notification.ReminderScheduler.scheduleDailyReminder(context, _reminderTime.value)
        } else {
            com.example.notification.ReminderScheduler.cancelDailyReminder(context)
        }
    }

    fun setReminderTime(context: android.content.Context, time: String) {
        _reminderTime.value = time
        prefs.edit().putString("reminder_time", time).apply()
        if (_dailyPrompt.value) {
            com.example.notification.ReminderScheduler.scheduleDailyReminder(context, time)
        }
    }

    fun completeOnboarding() {
        _hasCompletedOnboarding.value = true
        prefs.edit().putBoolean("has_completed_onboarding", true).apply()
    }

    fun setDailyGoalMinutes(minutes: Int) {
        val validated = minutes.coerceIn(5, 300)
        _dailyGoalMinutes.value = validated
        prefs.edit().putInt("daily_goal_minutes", validated).apply()
    }

    fun setDailyGoalPages(pages: Int) {
        val validated = pages.coerceIn(1, 200)
        _dailyGoalPages.value = validated
        prefs.edit().putInt("daily_goal_pages", validated).apply()
    }

    fun setDailyGoalType(type: String) {
        _dailyGoalType.value = type
        prefs.edit().putString("daily_goal_type", type).apply()
    }

    fun addBook(title: String, author: String, pages: Int) {
        viewModelScope.launch {
            repository.insertBook(Book(title = title, author = author, totalPages = pages, currentPage = 0))
        }
    }

    fun updateBookPages(book: Book, newPages: Int) {
        val updated = book.copy(
            currentPage = newPages.coerceIn(0, book.totalPages),
            isCompleted = newPages >= book.totalPages
        )
        viewModelScope.launch {
            repository.updateBook(updated)
        }
    }

    fun deleteBook(bookId: Int) {
        viewModelScope.launch {
            repository.deleteBook(bookId)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.deleteAllLogs()
        }
    }

    // Local-First Auth Methods
    fun loginUser(emailOrUsername: String, passwordRaw: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            when (val result = userRepository.loginUser(emailOrUsername, passwordRaw)) {
                is AuthResult.Success -> {
                    _authError.value = null
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _authError.value = result.message
                }
            }
        }
    }

    fun registerUser(
        username: String,
        email: String,
        passwordRaw: String,
        displayName: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _authError.value = null
            when (val result = userRepository.registerUser(username, email, passwordRaw, displayName)) {
                is AuthResult.Success -> {
                    _authError.value = null
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _authError.value = result.message
                }
            }
        }
    }

    fun logoutUser(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            userRepository.logout()
            onLoggedOut()
        }
    }

    fun updateUserDisplayName(newDisplayName: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            userRepository.updateUserProfile(user, newDisplayName)
        }
    }

    fun updateUserProfilePhoto(photoUri: String?) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            userRepository.updateUserProfilePhoto(user, photoUri)
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }
}

