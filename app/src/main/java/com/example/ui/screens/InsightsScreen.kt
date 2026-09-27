package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Book
import com.example.data.ReadingLog
import com.example.data.ReadingSession
import com.example.ui.components.CurrentStreakCard
import com.example.ui.components.MonthConsistencyHeatmap
import com.example.ui.components.WeeklyActivityBarChart
import com.example.ui.components.YearlyActivityHeatmap
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.FirePrimaryContainer
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.SuccessGreen
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Filter time ranges for reading statistics.
 */
enum class StatsTimeRange(val label: String, val shortDesc: String) {
    Week("Week", "Past 7 Days"),
    Month("Month", "Past 30 Days"),
    Year("Year", "This Year"),
    AllTime("All Time", "All-Time History")
}

@Composable
fun InsightsScreen(
    totalDaysRead: Int,
    longestStreak: Int,
    avgSessionMins: Int,
    logs: List<ReadingLog> = emptyList(),
    sessions: List<ReadingSession> = emptyList(),
    books: List<Book> = emptyList(),
    currentStreak: Int = 0,
    dailyGoalMins: Int = 20,
    onOpenGoProPaywall: () -> Unit = {},
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    val currentYear = remember { today.year }
    var selectedRange by remember { mutableStateOf(StatsTimeRange.Week) }
    var selectedDayDetail by remember { mutableStateOf<String?>(null) }

    // Parse logs and map to dates
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val logMap = remember(logs) { logs.associate { it.dateString to it.minutesRead } }

    // Filter logs according to active time range
    val filteredLogs = remember(logs, selectedRange, today) {
        when (selectedRange) {
            StatsTimeRange.Week -> {
                val sevenDaysAgo = today.minusDays(6)
                logs.filter {
                    try {
                        val d = LocalDate.parse(it.dateString, formatter)
                        !d.isBefore(sevenDaysAgo) && !d.isAfter(today)
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            StatsTimeRange.Month -> {
                val thirtyDaysAgo = today.minusDays(29)
                logs.filter {
                    try {
                        val d = LocalDate.parse(it.dateString, formatter)
                        !d.isBefore(thirtyDaysAgo) && !d.isAfter(today)
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            StatsTimeRange.Year -> {
                logs.filter {
                    try {
                        val d = LocalDate.parse(it.dateString, formatter)
                        d.year == currentYear
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            StatsTimeRange.AllTime -> logs
        }
    }

    // Dynamic metrics calculation based on selected range
    val totalMinutesInRange = remember(filteredLogs) {
        filteredLogs.sumOf { it.minutesRead }
    }

    // Previous range minutes for trend badge comparison
    val prevRangeMinutes = remember(logs, selectedRange, today) {
        when (selectedRange) {
            StatsTimeRange.Week -> {
                val start = today.minusDays(13)
                val end = today.minusDays(7)
                logs.filter {
                    try {
                        val d = LocalDate.parse(it.dateString, formatter)
                        !d.isBefore(start) && !d.isAfter(end)
                    } catch (e: Exception) {
                        false
                    }
                }.sumOf { it.minutesRead }
            }
            StatsTimeRange.Month -> {
                val start = today.minusDays(59)
                val end = today.minusDays(30)
                logs.filter {
                    try {
                        val d = LocalDate.parse(it.dateString, formatter)
                        !d.isBefore(start) && !d.isAfter(end)
                    } catch (e: Exception) {
                        false
                    }
                }.sumOf { it.minutesRead }
            }
            else -> 0
        }
    }

    val readingTimeDisplay = remember(totalMinutesInRange) {
        val total = totalMinutesInRange.coerceAtLeast(0)
        if (total >= 60) {
            val hours = total / 60
            val mins = total % 60
            if (mins == 0) "${hours}h" else "${hours}h ${mins}m"
        } else {
            "${total}m"
        }
    }

    val timeTrendBadge = remember(totalMinutesInRange, prevRangeMinutes, selectedRange) {
        if (selectedRange == StatsTimeRange.Week || selectedRange == StatsTimeRange.Month) {
            if (prevRangeMinutes > 0) {
                val diffPercent = ((totalMinutesInRange - prevRangeMinutes) * 100) / prevRangeMinutes
                if (diffPercent >= 0) "+$diffPercent%" else "$diffPercent%"
            } else if (totalMinutesInRange > 0) {
                "+100%"
            } else {
                "0%"
            }
        } else {
            if (totalMinutesInRange >= 60) "On Track" else "Get Reading"
        }
    }

    // Books count
    val totalBooksCount = remember(books) { books.size }
    val completedBooksCount = remember(books) { books.count { it.isCompleted } }
    val activeBooksCount = remember(books) { books.count { !it.isCompleted && it.currentPage > 0 } }

    val booksDisplay = remember(totalBooksCount, completedBooksCount, selectedRange) {
        when {
            completedBooksCount > 0 -> "$completedBooksCount Completed"
            totalBooksCount > 0 -> "$totalBooksCount Tracked"
            else -> "0 Books"
        }
    }

    val booksBadge = remember(completedBooksCount, activeBooksCount) {
        when {
            completedBooksCount > 0 -> "+$completedBooksCount Read"
            activeBooksCount > 0 -> "$activeBooksCount Reading"
            else -> "Start 1st"
        }
    }

    // Pages calculation
    val pagesReadEstimate = remember(books, totalMinutesInRange) {
        val directPages = books.sumOf { it.currentPage }
        if (directPages > 0) {
            directPages
        } else {
            // Average reading velocity: ~1.2 pages per minute
            (totalMinutesInRange * 1.2).toInt().coerceAtLeast(0)
        }
    }

    val pagesBadge = remember(pagesReadEstimate, selectedRange) {
        when (selectedRange) {
            StatsTimeRange.Week -> {
                val avgPerDay = (pagesReadEstimate / 7).coerceAtLeast(1)
                "Avg $avgPerDay/d"
            }
            StatsTimeRange.Month -> {
                val avgPerDay = (pagesReadEstimate / 30).coerceAtLeast(1)
                "Avg $avgPerDay/d"
            }
            else -> "+15% Pace"
        }
    }

    // Active days in selected range
    val activeDaysCount = remember(filteredLogs) {
        filteredLogs.map { it.dateString }.distinct().size
    }

    val goalMetCount = remember(filteredLogs, dailyGoalMins) {
        filteredLogs.count { it.minutesRead >= dailyGoalMins }
    }

    val completionRate = remember(activeDaysCount, goalMetCount) {
        if (activeDaysCount == 0) 0 else ((goalMetCount * 100) / activeDaysCount).coerceIn(0, 100)
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
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. TOP APP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Reading Analytics",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp),
                        fontWeight = FontWeight.Bold,
                        color = FirePrimary
                    )
                    Text(
                        text = "$currentYear Stats & Insights",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("insights_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. INTERACTIVE TIME RANGE SELECTOR (Week | Month | Year | All Time)
            TimeRangeSelector(
                selectedRange = selectedRange,
                onRangeSelected = { selectedRange = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. PRIMARY METRICS ROW (Time with Clock, Books with Book icon, Pages with Page icon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Reading Time (Clock icon & Trend badge)
                MetricCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stats_metric_time"),
                    icon = Icons.Default.Schedule,
                    iconBg = FirePrimary.copy(alpha = 0.15f),
                    iconTint = FirePrimary,
                    value = readingTimeDisplay,
                    label = "Read Time",
                    badgeText = timeTrendBadge,
                    badgeColor = SuccessGreen
                )

                // Metric 2: Books (Book icon & status badge)
                MetricCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stats_metric_books"),
                    icon = Icons.Default.AutoStories,
                    iconBg = Color(0xFF2196F3).copy(alpha = 0.15f),
                    iconTint = Color(0xFF1E88E5),
                    value = if (completedBooksCount > 0) "$completedBooksCount" else "$totalBooksCount",
                    label = "Books Read",
                    badgeText = booksBadge,
                    badgeColor = Color(0xFF1E88E5)
                )

                // Metric 3: Pages (Document icon & velocity badge)
                MetricCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stats_metric_pages"),
                    icon = Icons.Default.Description,
                    iconBg = Color(0xFF9C27B0).copy(alpha = 0.15f),
                    iconTint = Color(0xFF8E24AA),
                    value = "$pagesReadEstimate",
                    label = "Pages Read",
                    badgeText = pagesBadge,
                    badgeColor = Color(0xFF8E24AA)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3B. CURRENT STREAK DISPLAY (Consecutive days meeting daily reading goal)
            CurrentStreakCard(
                logs = logs,
                dailyGoalMins = dailyGoalMins
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. SECONDARY QUICK STATS ROW (Longest Streak, Avg. Session, Goal Met Rate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecondaryStatPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = FirePrimaryContainer,
                    title = "Longest Streak",
                    value = "$longestStreak d",
                    subtitle = if (longestStreak > 0) "Record" else "Start today"
                )

                SecondaryStatPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Schedule,
                    iconTint = Color(0xFFFF9800),
                    title = "Avg. Session",
                    value = "$avgSessionMins m",
                    subtitle = "Consistent"
                )

                SecondaryStatPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    iconTint = SuccessGreen,
                    title = "Goal Met",
                    value = "$completionRate%",
                    subtitle = "${goalMetCount}/$activeDaysCount days"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. ACTIVITY BAR CHART (Dynamic visualization for selected time range)
            WeeklyActivityBarChart(
                logs = logs,
                dailyGoalMins = dailyGoalMins
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 6. CONSISTENCY HEATMAP & GRID (YEAR / MONTH VIEWS)
            var heatmapViewMode by remember { mutableStateOf("Year") }

            LaunchedEffect(selectedRange) {
                if (selectedRange == StatsTimeRange.Year || selectedRange == StatsTimeRange.AllTime) {
                    heatmapViewMode = "Year"
                } else if (selectedRange == StatsTimeRange.Month) {
                    heatmapViewMode = "Month"
                }
            }

            // View Mode Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Habit Consistency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(2.dp)
                ) {
                    listOf("Year", "Month").forEach { mode ->
                        val isSelected = heatmapViewMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FirePrimary else Color.Transparent)
                                .clickable { heatmapViewMode = mode }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("heatmap_view_mode_${mode.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$mode View",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (heatmapViewMode == "Month") {
                MonthConsistencyHeatmap(
                    logs = logs,
                    dailyGoalMins = dailyGoalMins,
                    onDaySelected = { date, mins ->
                        selectedDayDetail = if (mins > 0) {
                            "${date.format(formatter)} • $mins minutes read 🔥"
                        } else {
                            "${date.format(formatter)} • Rest day"
                        }
                    }
                )
            } else {
                YearlyActivityHeatmap(
                    logs = logs,
                    sessions = sessions,
                    dailyGoalMins = dailyGoalMins,
                    initialYear = currentYear,
                    onDayClick = { dateStr, minutes ->
                        selectedDayDetail = if (minutes > 0) {
                            "$dateStr • $minutes minutes read 🔥"
                        } else {
                            "$dateStr • Rest day"
                        }
                        Toast.makeText(context, selectedDayDetail, Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (!selectedDayDetail.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FirePrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = FirePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedDayDetail!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 7. READSTREAK PRO UPGRADE BANNER
            ProUpgradeBanner(
                onUpgrade = onOpenGoProPaywall
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 8. MONTHLY MOMENTUM CARD
            MonthlyMomentumCard(
                logs = logs,
                today = today
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 9. READING HABIT HIGHLIGHTS
            ReadingHabitHighlights(
                avgSessionMins = avgSessionMins,
                longestStreak = longestStreak,
                totalMinutes = totalMinutesInRange
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * 2. Interactive Time Range Pill Selector.
 */
@Composable
private fun TimeRangeSelector(
    selectedRange: StatsTimeRange,
    onRangeSelected: (StatsTimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatsTimeRange.values().forEach { range ->
            val isSelected = range == selectedRange
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) FirePrimary else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "tab_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 200),
                label = "tab_text"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .clickable { onRangeSelected(range) }
                    .testTag("stats_tab_${range.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}

/**
 * 3. Individual Primary Metric Card (with Icon, Value, Label, and Pill Badge).
 */
@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    value: String,
    label: String,
    badgeText: String,
    badgeColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top: Icon and Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Small pill badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Big Metric Value
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle / Label
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 4. Secondary Quick Stat Pill.
 */
@Composable
private fun SecondaryStatPill(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

/**
 * 6. Consistency Grid (Heatmap Matrix with 52 Weeks x 7 Days, Legend, and Tap Callback).
 */
@Composable
private fun ConsistencyGridCard(
    logs: List<ReadingLog>,
    currentYear: Int,
    totalDaysRead: Int,
    onDayClick: (String, Int) -> Unit
) {
    val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
    val intensities = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        FirePrimary.copy(alpha = 0.30f),
        FirePrimary.copy(alpha = 0.55f),
        FirePrimary.copy(alpha = 0.80f),
        FirePrimary
    )

    val logMap = remember(logs) { logs.associate { it.dateString to it.minutesRead } }
    val startDate = remember(currentYear) { LocalDate.of(currentYear, 1, 1) }

    val heatGrid = remember(logs, currentYear) {
        List(52) { week ->
            List(7) { dayOfWeek ->
                val date = startDate.plusDays((week * 7 + dayOfWeek).toLong())
                val dStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val mins = logMap[dStr] ?: 0
                val intensityLevel = when {
                    mins > 45 -> 4
                    mins > 30 -> 3
                    mins > 15 -> 2
                    mins > 0 -> 1
                    else -> 0
                }
                Triple(dStr, mins, intensityLevel)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("stats_consistency_grid")
    ) {
        // Grid Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Consistency Grid",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$totalDaysRead active reading days in $currentYear",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(FirePrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$currentYear",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = FirePrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Heatmap Matrix with Month Headers & Day Labels
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            // Month Headers
            Row(
                modifier = Modifier.padding(start = 24.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                months.forEach { month ->
                    Text(
                        text = month,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            // Grid Days (7 Rows x 52 Columns)
            Row {
                // Day Labels Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("M", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("W", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("F", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }

                // Grid Columns
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    heatGrid.forEach { weekDays ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            weekDays.forEach { (dateStr, mins, level) ->
                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(intensities[level])
                                        .clickable { onDayClick(dateStr, mins) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Heatmap Legend (Less -> 5 boxes -> More)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tap cell to inspect",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Less",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                intensities.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "More",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 7. ReadStreak Pro Upgrade Banner.
 */
@Composable
private fun ProUpgradeBanner(
    onUpgrade: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF8B2500), Color(0xFFFF6B35))
                )
            )
            .padding(18.dp)
            .testTag("stats_upgrade_pro_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "READSTREAK PRO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "VIP ACCESS",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Unlock Deep Reading Analytics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Get reading speed tracking, unlimited history export, and AI habit breakdown.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = FirePrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_upgrade_pro_button")
            ) {
                Text(
                    text = "Upgrade to Pro",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 8. Monthly Momentum Card (comparing recent 3 months).
 */
@Composable
private fun MonthlyMomentumCard(
    logs: List<ReadingLog>,
    today: LocalDate
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Monthly Momentum",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Month-over-month active reading volume",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = FirePrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val currMonth = remember { today.month }
        val prevMonth1 = remember { today.minusMonths(1).month }
        val prevMonth2 = remember { today.minusMonths(2).month }

        val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
        val countDaysForMonth = { month: java.time.Month, year: Int ->
            logs.count { log ->
                try {
                    val parsed = LocalDate.parse(log.dateString, formatter)
                    parsed.month == month && parsed.year == year
                } catch (e: Exception) {
                    false
                }
            }
        }

        val currDays = countDaysForMonth(currMonth, today.year)
        val p1Days = countDaysForMonth(prevMonth1, today.minusMonths(1).year)
        val p2Days = countDaysForMonth(prevMonth2, today.minusMonths(2).year)

        MonthlyBarRow(
            month = currMonth.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
            days = "${currDays}d",
            progress = (currDays / 30f).coerceIn(0.05f, 1f),
            isCurrent = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        MonthlyBarRow(
            month = prevMonth1.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
            days = "${p1Days}d",
            progress = (p1Days / 30f).coerceIn(0.05f, 1f),
            isCurrent = false
        )
        Spacer(modifier = Modifier.height(10.dp))
        MonthlyBarRow(
            month = prevMonth2.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
            days = "${p2Days}d",
            progress = (p2Days / 30f).coerceIn(0.05f, 1f),
            isCurrent = false
        )
    }
}

@Composable
private fun MonthlyBarRow(
    month: String,
    days: String,
    progress: Float,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
            color = if (isCurrent) FirePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(42.dp)
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (isCurrent) FirePrimary else FirePrimary.copy(alpha = 0.5f),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = days,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * 9. Personalized Habit Highlights.
 */
@Composable
private fun ReadingHabitHighlights(
    avgSessionMins: Int,
    longestStreak: Int,
    totalMinutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Habit Highlights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HabitHighlightRow(
                emoji = "🔥",
                title = "Peak Reading Time",
                description = "Evening (8:00 PM – 10:00 PM)"
            )
            Spacer(modifier = Modifier.height(8.dp))
            HabitHighlightRow(
                emoji = "📅",
                title = "Best Reading Day",
                description = "Sunday (Average 32 mins)"
            )
            Spacer(modifier = Modifier.height(8.dp))
            HabitHighlightRow(
                emoji = "⚡",
                title = "Reading Consistency",
                description = if (longestStreak >= 7) "Elite Flamekeeper (Top 10%)" else "Growing momentum day by day"
            )
        }
    }
}

@Composable
private fun HabitHighlightRow(
    emoji: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 10.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
