package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.DailyReminderReceiver
import com.example.notification.ReminderScheduler
import com.example.ui.navigation.ReadStreakApp
import com.example.ui.theme.ReadStreakTheme
import com.example.viewmodel.ReadStreakViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ReadStreakViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create high-priority notification channel for daily reading reminders
        DailyReminderReceiver.createNotificationChannel(applicationContext)

        // Ensure active daily reminder is scheduled if enabled
        if (viewModel.dailyPrompt.value) {
            ReminderScheduler.scheduleDailyReminder(applicationContext, viewModel.reminderTime.value)
        }

        com.example.timer.ReadingTimerManager.updateStateFromPrefs(applicationContext)

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            ReadStreakTheme(themeMode = themeMode) {
                ReadStreakApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.example.timer.ReadingTimerManager.updateStateFromPrefs(applicationContext)
    }
}


