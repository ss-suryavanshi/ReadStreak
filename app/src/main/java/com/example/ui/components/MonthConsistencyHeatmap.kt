package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReadingLog
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.FirePrimaryContainer
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.SuccessGreen
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Visual Heatmap component displaying reading activity over a month to track habit consistency.
 */
@Composable
fun MonthConsistencyHeatmap(
    logs: List<ReadingLog>,
    dailyGoalMins: Int = 20,
    modifier: Modifier = Modifier,
    initialMonth: YearMonth = YearMonth.now(),
    onDaySelected: (LocalDate, Int) -> Unit = { _, _ -> }
) {
    val today = remember { LocalDate.now() }
    var currentYearMonth by remember { mutableStateOf(initialMonth) }
    var selectedDate by remember { mutableStateOf(today) }

    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val logMap = remember(logs) { logs.associate { it.dateString to it } }

    val daysInMonth = remember(currentYearMonth) { currentYearMonth.lengthOfMonth() }
    val firstDayOfMonth = remember(currentYearMonth) { currentYearMonth.atDay(1) }
    // 0 = Monday, 6 = Sunday
    val startDayOffset = remember(firstDayOfMonth) {
        firstDayOfMonth.dayOfWeek.value - 1
    }

    // Heat intensities matching app flame palette
    val heatColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), // 0: Rest/No activity
        FirePrimary.copy(alpha = 0.28f),                             // 1: 1-14 mins
        FirePrimary.copy(alpha = 0.55f),                             // 2: 15-29 mins
        FirePrimary.copy(alpha = 0.80f),                             // 3: 30-44 mins
        FirePrimary                                                  // 4: 45+ mins
    )

    // Compute monthly statistics
    val monthStats = remember(logs, currentYearMonth, dailyGoalMins) {
        var activeDays = 0
        var totalMinutes = 0
        var goalMetDays = 0
        val currentDaysCount = if (currentYearMonth == YearMonth.from(today)) {
            today.dayOfMonth
        } else if (currentYearMonth.isBefore(YearMonth.from(today))) {
            daysInMonth
        } else {
            0
        }

        for (day in 1..daysInMonth) {
            val date = currentYearMonth.atDay(day)
            val dStr = date.format(formatter)
            val log = logMap[dStr]
            if (log != null && log.minutesRead > 0) {
                activeDays++
                totalMinutes += log.minutesRead
                if (log.minutesRead >= dailyGoalMins) {
                    goalMetDays++
                }
            }
        }

        val consistencyRate = if (currentDaysCount > 0) {
            ((activeDays * 100) / currentDaysCount).coerceIn(0, 100)
        } else 0

        Triple(activeDays, totalMinutes, consistencyRate)
    }

    val activeDaysCount = monthStats.first
    val totalMonthMinutes = monthStats.second
    val consistencyPercentage = monthStats.third

    val totalHoursDisplay = remember(totalMonthMinutes) {
        if (totalMonthMinutes >= 60) {
            val h = totalMonthMinutes / 60
            val m = totalMonthMinutes % 60
            if (m == 0) "${h}h" else "${h}h ${m}m"
        } else {
            "${totalMonthMinutes}m"
        }
    }

    // Selected day detail
    val selectedLog = remember(selectedDate, logs) {
        val dStr = selectedDate.format(formatter)
        logMap[dStr]
    }
    val selectedMinutes = selectedLog?.minutesRead ?: 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("month_consistency_heatmap_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // 1. MONTH HEADER WITH NAVIGATION
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FirePrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = FirePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "${currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentYearMonth.year}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Monthly Consistency Heatmap",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Month Switcher Controls (< > and reset)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("month_heatmap_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (currentYearMonth != YearMonth.from(today)) {
                        TextButton(
                            onClick = {
                                currentYearMonth = YearMonth.from(today)
                                selectedDate = today
                            },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FirePrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("month_heatmap_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. MONTH METRICS BAR (Active Days, Consistency Rate, Total Reading Time)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active Days
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ACTIVE DAYS",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$activeDaysCount d",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Divider dot
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Consistency Rate
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CONSISTENCY",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = if (consistencyPercentage >= 70) SuccessGreen else FirePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$consistencyPercentage%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (consistencyPercentage >= 70) SuccessGreen else FirePrimary
                        )
                    }
                }

                // Divider dot
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Total Time
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TOTAL READ",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = totalHoursDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FirePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. WEEKDAYS HEADER (MON -> SUN)
            val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. CALENDAR HEATMAP GRID (7 Columns x Weeks)
            val totalCells = startDayOffset + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - startDayOffset + 1

                            if (cellIndex < startDayOffset || dayNumber > daysInMonth) {
                                // Empty spacer cell
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            } else {
                                val cellDate = currentYearMonth.atDay(dayNumber)
                                val dStr = cellDate.format(formatter)
                                val log = logMap[dStr]
                                val mins = log?.minutesRead ?: 0
                                val isCellToday = cellDate == today
                                val isCellSelected = cellDate == selectedDate
                                val isFuture = cellDate.isAfter(today)
                                val isGoalMet = mins >= dailyGoalMins

                                val intensityLevel = when {
                                    isFuture -> 0
                                    mins > 45 -> 4
                                    mins > 30 -> 3
                                    mins > 15 -> 2
                                    mins > 0 -> 1
                                    else -> 0
                                }

                                val cellBgColor by animateColorAsState(
                                    targetValue = if (isFuture) {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    } else {
                                        heatColors[intensityLevel]
                                    },
                                    animationSpec = tween(200),
                                    label = "cell_color"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cellBgColor)
                                        .border(
                                            width = if (isCellSelected) 2.dp else if (isCellToday) 1.5.dp else 0.5.dp,
                                            color = when {
                                                isCellSelected -> MaterialTheme.colorScheme.onSurface
                                                isCellToday -> FirePrimaryContainer
                                                else -> Color.Transparent
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable(enabled = !isFuture) {
                                            selectedDate = cellDate
                                            onDaySelected(cellDate, mins)
                                        }
                                        .testTag("month_heatmap_day_$dayNumber"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Day Number
                                    Text(
                                        text = "$dayNumber",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        fontWeight = if (isCellToday || isCellSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            intensityLevel >= 3 -> Color.White
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )

                                    // Small goal indicator dot on top right
                                    if (isGoalMet) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(3.dp)
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (intensityLevel >= 3) Color.White else FirePrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. HEATMAP INTENSITY LEGEND
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(FirePrimary)
                    )
                    Text(
                        text = "Dot = Goal met ($dailyGoalMins+ min)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "0m",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    heatColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = "45m+",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. SELECTED DAY INSPECTION BANNER
            SelectedDayInspectionCard(
                date = selectedDate,
                minutesRead = selectedMinutes,
                bookTitle = selectedLog?.bookTitle,
                dailyGoalMins = dailyGoalMins
            )
        }
    }
}

/**
 * Inspection detail box for the user-selected date in the month heatmap.
 */
@Composable
private fun SelectedDayInspectionCard(
    date: LocalDate,
    minutesRead: Int,
    bookTitle: String?,
    dailyGoalMins: Int
) {
    val dateDisplay = remember(date) {
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        "$dayOfWeek, $month ${date.dayOfMonth}, ${date.year}"
    }

    val isGoalMet = minutesRead >= dailyGoalMins
    val isToday = date == LocalDate.now()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (minutesRead > 0) FirePrimary.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
            .border(
                1.dp,
                if (minutesRead > 0) FirePrimary.copy(alpha = 0.25f) else OutlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateDisplay,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isToday) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FirePrimaryContainer)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "TODAY",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (minutesRead > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = FirePrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$minutesRead mins read",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = FirePrimary
                    )
                    if (!bookTitle.isNullOrEmpty()) {
                        Text(
                            text = " • $bookTitle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            } else {
                Text(
                    text = "No reading logged for this day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Status badge
        if (minutesRead > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isGoalMet) SuccessGreen.copy(alpha = 0.15f) else FirePrimary.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isGoalMet) Icons.Default.CheckCircle else Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (isGoalMet) SuccessGreen else FirePrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isGoalMet) "Goal Met" else "Logged",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isGoalMet) SuccessGreen else FirePrimary
                    )
                }
            }
        }
    }
}
