package com.example.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReadingTimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val totalMinutes: Int = 20,
    val remainingSeconds: Long = 0L,
    val bookTitle: String = "Daily Reading",
    val endTimestamp: Long = 0L
)

object ReadingTimerManager {

    private val _timerState = MutableStateFlow(ReadingTimerState())
    val timerState: StateFlow<ReadingTimerState> = _timerState.asStateFlow()

    fun updateStateFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences("reading_timer_prefs", Context.MODE_PRIVATE)
        val isRunning = prefs.getBoolean("is_running", false)
        val isPaused = prefs.getBoolean("is_paused", false)
        val isFinished = prefs.getBoolean("is_finished", false)
        val totalMinutes = prefs.getInt("total_minutes", 20)
        val bookTitle = prefs.getString("book_title", "Daily Reading") ?: "Daily Reading"
        val endTimestamp = prefs.getLong("end_timestamp", 0L)
        val pausedRemainingSecs = prefs.getLong("paused_remaining_secs", 0L)

        val remainingSecs = when {
            isFinished -> 0L
            isPaused -> pausedRemainingSecs
            isRunning -> {
                val remMs = (endTimestamp - System.currentTimeMillis()).coerceAtLeast(0L)
                remMs / 1000
            }
            else -> totalMinutes * 60L
        }

        val actualRunning = isRunning && remainingSecs > 0

        _timerState.value = ReadingTimerState(
            isRunning = actualRunning,
            isPaused = isPaused,
            isFinished = isFinished || (isRunning && remainingSecs <= 0),
            totalMinutes = totalMinutes,
            remainingSeconds = remainingSecs,
            bookTitle = bookTitle,
            endTimestamp = endTimestamp
        )
    }

    fun startTimer(context: Context, totalMinutes: Int, bookTitle: String) {
        val prefs = context.getSharedPreferences("reading_timer_prefs", Context.MODE_PRIVATE)
        val endTimestamp = System.currentTimeMillis() + (totalMinutes * 60 * 1000L)

        prefs.edit()
            .putBoolean("is_running", true)
            .putBoolean("is_paused", false)
            .putBoolean("is_finished", false)
            .putInt("total_minutes", totalMinutes)
            .putString("book_title", bookTitle)
            .putLong("end_timestamp", endTimestamp)
            .putLong("paused_remaining_secs", 0L)
            .apply()

        // Schedule Exact Alarm so it triggers even when phone is locked
        scheduleAlarm(context, endTimestamp)

        // Start Foreground Service for ongoing lockscreen notification
        startForegroundService(context)

        updateStateFromPrefs(context)
    }

    fun pauseTimer(context: Context) {
        val prefs = context.getSharedPreferences("reading_timer_prefs", Context.MODE_PRIVATE)
        val endTimestamp = prefs.getLong("end_timestamp", 0L)
        val remSecs = ((endTimestamp - System.currentTimeMillis()).coerceAtLeast(0L)) / 1000

        cancelAlarm(context)
        stopForegroundService(context)

        prefs.edit()
            .putBoolean("is_running", false)
            .putBoolean("is_paused", true)
            .putLong("paused_remaining_secs", remSecs)
            .apply()

        updateStateFromPrefs(context)
    }

    fun resumeTimer(context: Context) {
        val prefs = context.getSharedPreferences("reading_timer_prefs", Context.MODE_PRIVATE)
        val pausedSecs = prefs.getLong("paused_remaining_secs", 0L)
        val totalMins = prefs.getInt("total_minutes", 20)
        val bookTitle = prefs.getString("book_title", "Daily Reading") ?: "Daily Reading"

        if (pausedSecs <= 0) {
            startTimer(context, totalMins, bookTitle)
            return
        }

        val newEndTimestamp = System.currentTimeMillis() + (pausedSecs * 1000L)

        prefs.edit()
            .putBoolean("is_running", true)
            .putBoolean("is_paused", false)
            .putLong("end_timestamp", newEndTimestamp)
            .putLong("paused_remaining_secs", 0L)
            .apply()

        scheduleAlarm(context, newEndTimestamp)
        startForegroundService(context)

        updateStateFromPrefs(context)
    }

    fun cancelTimer(context: Context) {
        cancelAlarm(context)
        stopForegroundService(context)

        val prefs = context.getSharedPreferences("reading_timer_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_running", false)
            .putBoolean("is_paused", false)
            .putBoolean("is_finished", false)
            .putLong("paused_remaining_secs", 0L)
            .apply()

        updateStateFromPrefs(context)
    }

    fun clearFinishedState(context: Context) {
        val prefs = context.getSharedPreferences("reading_timer_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_finished", false)
            .apply()
        updateStateFromPrefs(context)
    }

    fun notifyStateChanged(context: Context) {
        updateStateFromPrefs(context)
    }

    private fun scheduleAlarm(context: Context, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReadingTimerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback if exact alarm permission is disabled
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReadingTimerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun startForegroundService(context: Context) {
        val serviceIntent = Intent(context, ReadingTimerService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopForegroundService(context: Context) {
        val serviceIntent = Intent(context, ReadingTimerService::class.java)
        context.stopService(serviceIntent)
    }
}
