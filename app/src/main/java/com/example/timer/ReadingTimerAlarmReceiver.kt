package com.example.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class ReadingTimerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Stop foreground service if running
        val stopServiceIntent = Intent(context, ReadingTimerService::class.java).apply {
            action = ReadingTimerService.ACTION_STOP
        }
        context.stopService(stopServiceIntent)

        val prefs = context.getSharedPreferences("reading_timer_prefs", Context.MODE_PRIVATE)
        val minutes = prefs.getInt("total_minutes", 20)
        val bookTitle = prefs.getString("book_title", "Daily Reading") ?: "Daily Reading"

        // Mark timer as finished and save completed session info
        prefs.edit()
            .putBoolean("is_running", false)
            .putBoolean("is_paused", false)
            .putBoolean("is_finished", true)
            .putLong("finished_at", System.currentTimeMillis())
            .apply()

        // Ring Alarm Sound & Vibrate
        playAlarmSoundAndVibrate(context)

        // Show High Priority Heads-up Alarm Notification
        showTimerFinishedNotification(context, minutes, bookTitle)

        // Notify active state flows
        ReadingTimerManager.notifyStateChanged(context)
    }

    private fun playAlarmSoundAndVibrate(context: Context) {
        try {
            // Alarm Vibration pattern
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (vibrator.hasVibrator()) {
                val pattern = longArrayOf(0, 500, 250, 500, 250, 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showTimerFinishedNotification(context: Context, minutes: Int, bookTitle: String) {
        val channelId = "reading_timer_alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Reading Timer Completion Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Rings an alarm notification when your reading session timer expires"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 1000)
                setSound(alarmUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TIMER_CLAIM_DIALOG", true)
            putExtra("CLAIM_MINUTES", minutes)
            putExtra("CLAIM_BOOK", bookTitle)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val offer = com.example.notification.NotificationAdManager.getRandomOffer()
        val hasAds = com.example.notification.NotificationAdManager.isNotificationAdsEnabled(context)

        val bigMessage = if (hasAds) {
            "Time's up! You completed your $minutes minute reading session for '$bookTitle'. Tap now to claim your streak progress!\n\n🎁 [Featured Reward] ${offer.brandName}: ${offer.headline}"
        } else {
            "Time's up! You completed your $minutes minute reading session for '$bookTitle'. Tap now to claim your streak progress!"
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Reading Timer Finished! 📖⏰")
            .setContentText("Completed $minutes mins for '$bookTitle'! ${if (hasAds) "• Sponsored: ${offer.brandName}" else ""}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigMessage)
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true)

        if (hasAds) {
            com.example.notification.NotificationAdManager.attachSponsoredAdToNotification(context, builder, offer)
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        const val NOTIFICATION_ID = 8888
    }
}
