package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        createNotificationChannel(context)

        // Reschedule daily reminder on device boot if enabled
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isEnabled = prefs.getBoolean(KEY_ENABLED, true)
            val savedTime = prefs.getString(KEY_TIME, "8:00 PM") ?: "8:00 PM"
            if (isEnabled) {
                ReminderScheduler.scheduleDailyReminder(context, savedTime)
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(context)
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val todayLog = database.readingDao().getLogForDate(todayStr)

            val appPrefs = context.getSharedPreferences("read_streak_app_prefs", Context.MODE_PRIVATE)
            val goalMinutes = appPrefs.getInt("daily_goal_minutes", 20)
            val goalPages = appPrefs.getInt("daily_goal_pages", 15)
            val goalType = appPrefs.getString("daily_goal_type", "MINUTES") ?: "MINUTES"

            val minutesRead = todayLog?.minutesRead ?: 0
            val pagesRead = minutesRead // estimated pages read based on time logged

            val isGoalMet = when (goalType) {
                "PAGES" -> pagesRead >= goalPages
                "BOTH" -> minutesRead >= goalMinutes && pagesRead >= goalPages
                else -> minutesRead >= goalMinutes
            }

            // Prompt user ONLY if they haven't met their daily reading goal yet
            if (!isGoalMet) {
                showDailyReminderNotification(
                    context = context,
                    minutesRead = minutesRead,
                    pagesRead = pagesRead,
                    goalMinutes = goalMinutes,
                    goalPages = goalPages,
                    goalType = goalType
                )
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "daily_reading_reminder"
        const val NOTIFICATION_ID = 1001
        const val PREFS_NAME = "read_streak_notification_prefs"
        const val KEY_ENABLED = "daily_prompt_enabled"
        const val KEY_TIME = "reminder_time"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Daily Reading Reminder",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Daily push notifications prompting you to complete your reading goal and protect your streak."
                    enableVibration(true)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun showDailyReminderNotification(
            context: Context,
            minutesRead: Int = 0,
            pagesRead: Int = 0,
            goalMinutes: Int = 20,
            goalPages: Int = 15,
            goalType: String = "MINUTES",
            isTest: Boolean = false
        ) {
            createNotificationChannel(context)

            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = if (isTest) {
                "Notification Test: Goal Reminder 🔔"
            } else if (minutesRead == 0 && pagesRead == 0) {
                "Protect Your Reading Streak! 🔥"
            } else {
                "Almost There! Complete Daily Reading Goal 📖"
            }

            val bodyText = when {
                goalType == "PAGES" && pagesRead == 0 ->
                    "You haven't logged any reading today yet. Read $goalPages pages before bedtime to keep your streak!"
                goalType == "PAGES" ->
                    "Logged $pagesRead/$goalPages pages so far today. Read ${goalPages - pagesRead} more pages to hit your goal!"
                goalType == "BOTH" && (minutesRead == 0 || pagesRead == 0) ->
                    "Your daily goal is $goalMinutes mins & $goalPages pages. Log a quick session now to protect your streak!"
                minutesRead == 0 ->
                    "You haven't completed your $goalMinutes min reading goal today. Log a few pages now!"
                else ->
                    "You've read $minutesRead/$goalMinutes mins today. Read ${goalMinutes - minutesRead} more mins to hit your goal!"
            }

            val offer = NotificationAdManager.getRandomOffer()
            val hasAds = NotificationAdManager.isNotificationAdsEnabled(context)

            val fullBigText = if (hasAds) {
                "$bodyText\n\n🎁 [Sponsored Recommendation] ${offer.brandName}: ${offer.headline}\nTap action below to claim your reader deal!"
            } else {
                "$bodyText\n\nTap to open Read Streak and record your session now."
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(if (hasAds) "$bodyText • Sponsored: ${offer.brandName}" else bodyText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(fullBigText)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            if (hasAds) {
                NotificationAdManager.attachSponsoredAdToNotification(context, builder, offer)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }
}

object ReminderScheduler {

    fun scheduleDailyReminder(context: Context, timeString: String) {
        // Save state to SharedPreferences
        val prefs = context.getSharedPreferences(DailyReminderReceiver.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(DailyReminderReceiver.KEY_ENABLED, true)
            .putString(DailyReminderReceiver.KEY_TIME, timeString)
            .apply()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            var hour = 20
            var minute = 0

            try {
                val parts = timeString.split(":")
                if (parts.size >= 2) {
                    val hourPart = parts[0].trim().toIntOrNull() ?: 8
                    val minAndMeridiem = parts[1].trim().split(" ")
                    minute = minAndMeridiem.getOrNull(0)?.toIntOrNull() ?: 0
                    val meridiem = minAndMeridiem.getOrNull(1)?.uppercase(Locale.US)
                    
                    hour = when {
                        meridiem == "PM" && hourPart < 12 -> hourPart + 12
                        meridiem == "AM" && hourPart == 12 -> 0
                        meridiem == null && hourPart <= 23 -> hourPart
                        else -> hourPart
                    }
                }
            } catch (e: Exception) {
                hour = 20
                minute = 0
            }

            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If scheduled time has already passed for today, set for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelDailyReminder(context: Context) {
        val prefs = context.getSharedPreferences(DailyReminderReceiver.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(DailyReminderReceiver.KEY_ENABLED, false).apply()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun sendTestNotification(context: Context) {
        val appPrefs = context.getSharedPreferences("read_streak_app_prefs", Context.MODE_PRIVATE)
        val goalMinutes = appPrefs.getInt("daily_goal_minutes", 20)
        val goalPages = appPrefs.getInt("daily_goal_pages", 15)
        val goalType = appPrefs.getString("daily_goal_type", "MINUTES") ?: "MINUTES"

        DailyReminderReceiver.showDailyReminderNotification(
            context = context,
            minutesRead = 0,
            pagesRead = 0,
            goalMinutes = goalMinutes,
            goalPages = goalPages,
            goalType = goalType,
            isTest = true
        )
    }
}

