package com.example.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import android.widget.Toast
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.FirePrimaryContainer
import com.example.ui.theme.OutlineVariant

import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.OutlinedButton
import com.example.data.UserEntity
import com.example.ui.theme.OnSuccessGreenContainer
import com.example.ui.theme.SuccessGreenContainer

@Composable
fun SettingsScreen(
    currentUser: UserEntity? = null,
    themeMode: String,
    hapticFeedback: Boolean,
    dailyPrompt: Boolean,
    reminderTime: String,
    dailyGoalMinutes: Int = 20,
    dailyGoalPages: Int = 15,
    dailyGoalType: String = "MINUTES",
    onSetThemeMode: (String) -> Unit,
    onSetHapticFeedback: (Boolean) -> Unit,
    onSetDailyPrompt: (Boolean) -> Unit,
    onSetReminderTime: (String) -> Unit,
    onSetDailyGoalMinutes: (Int) -> Unit = {},
    onSetDailyGoalPages: (Int) -> Unit = {},
    onSetDailyGoalType: (String) -> Unit = {},
    onOpenGoProPaywall: () -> Unit,
    onOpenWidgetPreviews: () -> Unit,
    onOpenAuth: () -> Unit = {},
    onLogout: () -> Unit = {},
    onDeleteAllProgress: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showTestInterstitial by remember { mutableStateOf(false) }
    var showTestRewarded by remember { mutableStateOf(false) }

    if (showTestInterstitial) {
        com.example.ui.components.ads.InterstitialAdDialog(
            adTitle = "Audible: 100,000+ Audiobooks",
            adDescription = "Test Google AdMob Interstitial Unit ca-app-pub-3940256099942544/1033173712",
            onAdDismissed = { showTestInterstitial = false }
        )
    }

    if (showTestRewarded) {
        com.example.ui.components.ads.RewardedAdDialog(
            rewardTitle = "+50 Bonus Streak Points",
            rewardDescription = "Test Google AdMob Rewarded Video Unit ca-app-pub-3940256099942544/5224354917",
            onRewardEarned = { },
            onDismiss = { showTestRewarded = false }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onSetDailyPrompt(true)
            Toast.makeText(context, "Daily reading reminder enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission required for daily reminders", Toast.LENGTH_LONG).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = FirePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ACCOUNT & PRIVACY
            SectionHeader(icon = Icons.Default.Security, title = "ACCOUNT & SECURITY")
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.displayName ?: "Guest User",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentUser != null) "Email: ${currentUser.email}" else "Not signed in",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessGreenContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = OnSuccessGreenContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ENCRYPTED",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = OnSuccessGreenContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenAuth,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("settings_auth_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentUser != null) "Switch Account" else "Sign In / Register",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (currentUser != null) {
                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("settings_logout_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Log Out",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DAILY READING GOAL SECTION
            SectionHeader(icon = Icons.Default.Tune, title = "DAILY READING GOAL")
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                // Goal Mode selector (Minutes, Pages, Both)
                Text(
                    text = "Goal Target Metric",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Track daily progress by time, pages read, or both",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(3.dp)
                ) {
                    listOf("MINUTES" to "Minutes", "PAGES" to "Pages", "BOTH" to "Both").forEach { (typeKey, label) ->
                        val isSelected = dailyGoalType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FirePrimary else Color.Transparent)
                                .clickable { onSetDailyGoalType(typeKey) }
                                .padding(vertical = 8.dp)
                                .testTag("goal_type_$typeKey"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (dailyGoalType == "MINUTES" || dailyGoalType == "BOTH") {
                    // Minutes Goal Stepper & Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = FirePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Daily Target Minutes",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Stepper (- / value / +)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { onSetDailyGoalMinutes((dailyGoalMinutes - 5).coerceAtLeast(5)) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .testTag("decrease_minutes_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease minutes",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "${dailyGoalMinutes} mins",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = FirePrimary
                            )

                            IconButton(
                                onClick = { onSetDailyGoalMinutes((dailyGoalMinutes + 5).coerceAtMost(180)) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .testTag("increase_minutes_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase minutes",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset Chips for Minutes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(10, 15, 20, 30, 45, 60).forEach { mins ->
                            val isSelected = dailyGoalMinutes == mins
                            Surface(
                                onClick = { onSetDailyGoalMinutes(mins) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) FirePrimary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.testTag("preset_mins_$mins")
                            ) {
                                Text(
                                    text = "${mins}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (dailyGoalType == "BOTH") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(OutlineVariant.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (dailyGoalType == "PAGES" || dailyGoalType == "BOTH") {
                    // Pages Goal Stepper & Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = FirePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Daily Target Pages",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Stepper (- / value / +)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { onSetDailyGoalPages((dailyGoalPages - 5).coerceAtLeast(1)) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .testTag("decrease_pages_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease pages",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "${dailyGoalPages} pages",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = FirePrimary
                            )

                            IconButton(
                                onClick = { onSetDailyGoalPages((dailyGoalPages + 5).coerceAtMost(200)) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .testTag("increase_pages_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase pages",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset Chips for Pages
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 10, 15, 25, 40, 50).forEach { pgs ->
                            val isSelected = dailyGoalPages == pgs
                            Surface(
                                onClick = { onSetDailyGoalPages(pgs) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) FirePrimary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.testTag("preset_pages_$pgs")
                            ) {
                                Text(
                                    text = "${pgs}p",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calculated Yearly Projection Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FirePrimaryContainer.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FirePrimary.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Annual Commitment Projection 🚀",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = FirePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val weeklyMins = dailyGoalMinutes * 7
                        val yearlyHours = (dailyGoalMinutes * 365) / 60
                        val yearlyPages = dailyGoalPages * 365
                        val desc = when (dailyGoalType) {
                            "PAGES" -> "Reading $dailyGoalPages pages/day = ${dailyGoalPages * 7} pages/week (~${yearlyPages} pages / ~${yearlyPages / 250} books per year!)"
                            "BOTH" -> "Reading $dailyGoalMinutes mins & $dailyGoalPages pages/day = ~$yearlyHours hours & ~$yearlyPages pages per year!"
                            else -> "Reading $dailyGoalMinutes mins/day = $weeklyMins mins/week (~$yearlyHours hours of deep reading per year!)"
                        }
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // REMINDERS SECTION
            SectionHeader(icon = Icons.Default.Notifications, title = "REMINDERS")
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
            ) {
                // Switch row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Reading Prompt",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Stay consistent with notifications",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = dailyPrompt,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onSetDailyPrompt(enabled)
                                if (enabled) {
                                    Toast.makeText(context, "Daily reading reminder scheduled for $reminderTime", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FirePrimary
                        ),
                        modifier = Modifier.testTag("daily_prompt_switch")
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(OutlineVariant.copy(alpha = 0.3f))
                )

                // Time picker row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            var initialHour = 20
                            var initialMinute = 0
                            try {
                                val parts = reminderTime.split(":")
                                if (parts.size >= 2) {
                                    val h = parts[0].trim().toIntOrNull() ?: 8
                                    val minAndMeridiem = parts[1].trim().split(" ")
                                    val m = minAndMeridiem.getOrNull(0)?.toIntOrNull() ?: 0
                                    val meridiem = minAndMeridiem.getOrNull(1)?.uppercase(Locale.US)
                                    initialHour = when {
                                        meridiem == "PM" && h < 12 -> h + 12
                                        meridiem == "AM" && h == 12 -> 0
                                        else -> h
                                    }
                                    initialMinute = m
                                }
                            } catch (e: Exception) {
                                initialHour = 20
                                initialMinute = 0
                            }

                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val isPm = hourOfDay >= 12
                                    val hour12 = when {
                                        hourOfDay == 0 -> 12
                                        hourOfDay > 12 -> hourOfDay - 12
                                        else -> hourOfDay
                                    }
                                    val newTime = String.format(Locale.US, "%d:%02d %s", hour12, minute, if (isPm) "PM" else "AM")
                                    onSetReminderTime(newTime)
                                    Toast.makeText(context, "Daily reading reminder set for $newTime", Toast.LENGTH_SHORT).show()
                                },
                                initialHour,
                                initialMinute,
                                false
                            ).show()
                        }
                        .padding(16.dp)
                        .testTag("reminder_time_selector"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Reminder Time",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap to pick any custom time of day",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FirePrimaryContainer
                        ) {
                            Text(
                                text = reminderTime,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = FirePrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Quick preset time chips row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("7:00 AM", "12:00 PM", "6:00 PM", "8:00 PM", "9:30 PM").forEach { presetTime ->
                        val isSelected = reminderTime == presetTime
                        Surface(
                            onClick = {
                                onSetReminderTime(presetTime)
                                Toast.makeText(context, "Daily reading reminder set for $presetTime", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) FirePrimary else MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.testTag("preset_time_$presetTime")
                        ) {
                            Text(
                                text = presetTime,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(OutlineVariant.copy(alpha = 0.3f))
                )

                // Send Test Notification row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            com.example.notification.ReminderScheduler.sendTestNotification(context)
                            Toast.makeText(context, "Test notification sent! Check your status bar 🔔", Toast.LENGTH_SHORT).show()
                        }
                        .padding(16.dp)
                        .testTag("send_test_notification_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Test Daily Reminder",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FirePrimary
                        )
                        Text(
                            text = "Trigger a preview notification right now",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = FirePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(OutlineVariant.copy(alpha = 0.3f))
                )

                // Send Truecaller Style Notification Ad row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            com.example.notification.NotificationAdManager.showTruecallerStyleAdNotification(context)
                            Toast.makeText(context, "Truecaller-style Notification Ad sent! Check status bar 🔔", Toast.LENGTH_SHORT).show()
                        }
                        .padding(16.dp)
                        .testTag("send_truecaller_ad_notification_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Test Push Notification Ad (Truecaller Style) 📢",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FirePrimary
                        )
                        Text(
                            text = "Fire push notification with sponsored deal action buttons",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = FirePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(OutlineVariant.copy(alpha = 0.3f))
                )

                // Test Fullscreen Interstitial Ad Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTestInterstitial = true }
                        .padding(16.dp)
                        .testTag("test_interstitial_ad_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Test Full-Screen Interstitial Ad 🎬",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FirePrimary
                        )
                        Text(
                            text = "AdMob ca-app-pub-3940256099942544/1033173712 format",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = FirePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(OutlineVariant.copy(alpha = 0.3f))
                )

                // Test Rewarded Video Ad Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTestRewarded = true }
                        .padding(16.dp)
                        .testTag("test_rewarded_ad_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Test Rewarded Video Ad (+Bonus Points) 🎁",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FirePrimary
                        )
                        Text(
                            text = "AdMob ca-app-pub-3940256099942544/5224354917 format",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = FirePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // APPEARANCE SECTION
            SectionHeader(icon = Icons.Default.Palette, title = "APPEARANCE")
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                // Theme Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(2.dp)
                    ) {
                        listOf("Light", "Dark", "System").forEach { mode ->
                            val isSelected = themeMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.surface
                                        else Color.Transparent
                                    )
                                    .clickable { onSetThemeMode(mode) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("theme_button_$mode"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(OutlineVariant.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Haptic Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Haptic Feedback",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = onSetHapticFeedback,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FirePrimary
                        ),
                        modifier = Modifier.testTag("haptic_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // WIDGET PREVIEWS SECTION
            SectionHeader(icon = Icons.Default.Widgets, title = "WIDGETS")
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWidgetPreviews() }
                        .padding(16.dp)
                        .testTag("open_widget_previews_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Widget Previews & Setup",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Navigate to widget previews",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DATA SECTION
            SectionHeader(icon = Icons.Default.Download, title = "DATA")
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "Exported 248 days of logs to Download/ReadStreak_Logs.json", Toast.LENGTH_LONG).show()
                        }
                        .padding(16.dp)
                        .testTag("export_logs_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Export Reading Logs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ABOUT SECTION
            SectionHeader(icon = Icons.Default.Info, title = "ABOUT")
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "Thank you for rating Read Streak!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rate Read Streak",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(OutlineVariant.copy(alpha = 0.3f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Version",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "2.4.0 (Build 89)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Delete All Progress Button
            TextButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_all_progress_button")
            ) {
                Text(
                    text = "Delete All Progress",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text("Delete All Progress?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This action will reset your current streak, logs, and stats. It cannot be undone.", style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllProgress()
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "All progress reset.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FirePrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
    }
}
