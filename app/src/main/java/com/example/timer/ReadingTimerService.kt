package com.example.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class ReadingTimerService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val prefs = getSharedPreferences("reading_timer_prefs", Context.MODE_PRIVATE)
        val endTimestamp = prefs.getLong("end_timestamp", 0L)
        val totalMinutes = prefs.getInt("total_minutes", 20)
        val bookTitle = prefs.getString("book_title", "Daily Reading") ?: "Daily Reading"

        createNotificationChannel()

        val notification = buildOngoingNotification(endTimestamp, totalMinutes, bookTitle)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startNotificationTicker(endTimestamp, totalMinutes, bookTitle)

        return START_STICKY
    }

    private fun startNotificationTicker(endTimestamp: Long, totalMinutes: Int, bookTitle: String) {
        updateRunnable?.let { handler.removeCallbacks(it) }

        updateRunnable = object : Runnable {
            override fun run() {
                val remainingMs = (endTimestamp - System.currentTimeMillis()).coerceAtLeast(0L)
                val prefs = getSharedPreferences("reading_timer_prefs", Context.MODE_PRIVATE)
                val isRunning = prefs.getBoolean("is_running", false)
                val isPaused = prefs.getBoolean("is_paused", false)

                if (!isRunning || isPaused || remainingMs <= 0L) {
                    if (remainingMs <= 0L && isRunning) {
                        // Timer expired
                        stopSelf()
                    }
                    return
                }

                val notification = buildOngoingNotification(endTimestamp, totalMinutes, bookTitle)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)

                handler.postDelayed(this, 1000L)
            }
        }

        handler.post(updateRunnable!!)
    }

    private fun buildOngoingNotification(endTimestamp: Long, totalMinutes: Int, bookTitle: String): android.app.Notification {
        val remainingMs = (endTimestamp - System.currentTimeMillis()).coerceAtLeast(0L)
        val remainingSecs = remainingMs / 1000
        val mins = remainingSecs / 60
        val secs = remainingSecs % 60
        val formattedTime = String.format("%02d:%02d", mins, secs)

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            1002,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ReadingTimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1003,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Reading Session in Progress 📖 ($formattedTime)")
            .setContentText("Reading '$bookTitle' ($totalMinutes mins total). Keep standard posture!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Time remaining: $formattedTime\nReading '$bookTitle'. Timer runs accurately even when phone is locked.")
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel Timer", stopPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ongoing Reading Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active countdown while reading session is running in background or locked screen"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "reading_timer_ongoing_channel"
        const val NOTIFICATION_ID = 9999
        const val ACTION_STOP = "com.example.timer.ACTION_STOP_SERVICE"
    }
}
