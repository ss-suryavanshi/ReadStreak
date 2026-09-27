package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReadingLog
import com.example.data.ReadingSession
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.FirePrimaryContainer
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Filter period inside the Yearly Activity Heatmap.
 */
enum class HeatmapYearFilter(val label: String, val months: Set<Int>) {
    FullYear("Full Year", (1..12).toSet()),
    Q1("Q1 • Jan–Mar", setOf(1, 2, 3)),
    Q2("Q2 • Apr–Jun", setOf(4, 5, 6)),
    Q3("Q3 • Jul–Sep", setOf(7, 8, 9)),
    Q4("Q4 • Oct–Dec", setOf(10, 11, 12))
}

/**
 * Data holder for a single day cell in the 53-week x 7-day yearly heatmap grid.
 */
data class YearlyHeatmapCell(
    val date: LocalDate,
    val dateString: String,
    val isInTargetYear: Boolean,
    val isInFilterPeriod: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean,
    val minutesRead: Int,
    val pagesRead: Int,
    val bookTitle: String?,
    val notes: String?,
    val isGoalMet: Boolean,
    val intensityLevel: Int // 0..4
)

/**
 * Interactive Yearly Activity Heatmap showing 365/366 days of reading activity
 * arranged by calendar weeks (Mon-Sun), with year switching, quarter filtering,
 * monthly activity distribution bars, and day-level inspection.
 */
@Composable
fun YearlyActivityHeatmap(
    logs: List<ReadingLog>,
    sessions: List<ReadingSession> = emptyList(),
    dailyGoalMins: Int = 20,
    initialYear: Int = LocalDate.now().year,
    modifier: Modifier = Modifier,
    onDayClick: (String, Int) -> Unit = { _, _ -> }
) {
    val today = remember { LocalDate.now() }
    var selectedYear by remember { mutableIntStateOf(initialYear) }
    var selectedFilter by remember { mutableStateOf(HeatmapYearFilter.FullYear) }
    var selectedDate by remember { mutableStateOf(today) }

    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Aggregate daily logs and sessions by dateString
    val dailyDataMap = remember(logs, sessions) {
        val map = mutableMapOf<String, ReadingLog>()
        for (log in logs) {
            val existing = map[log.dateString]
            if (existing == null) {
                map[log.dateString] = log
            } else {
                map[log.dateString] = existing.copy(
                    minutesRead = existing.minutesRead + log.minutesRead,
                    pagesRead = existing.pagesRead + log.pagesRead,
                    bookTitle = if (log.bookTitle.isNotBlank()) log.bookTitle else existing.bookTitle,
                    notes = if (log.notes.isNotBlank()) log.notes else existing.notes
                )
            }
        }
        // Ensure any standalone sessions not yet in logs are also reflected
        val loggedDates = map.keys.toSet()
        for (session in sessions) {
            if (session.dateString !in loggedDates) {
                val prev = map[session.dateString]
                map[session.dateString] = ReadingLog(
                    dateString = session.dateString,
                    minutesRead = (prev?.minutesRead ?: 0) + session.minutesRead,
                    pagesRead = (prev?.pagesRead ?: 0) + session.pagesRead,
                    bookTitle = session.bookTitle,
                    notes = session.notes
                )
            }
        }
        map
    }

    val sessionsByDate = remember(sessions) {
        sessions.groupBy { it.dateString }
    }

    // 5-level Flame / Amber / Warm Terracotta heat palette
    val heatColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), // 0: 0 mins (Rest)
        FirePrimary.copy(alpha = 0.28f),                              // 1: Light reading
        FirePrimary.copy(alpha = 0.52f),                              // 2: Moderate reading
        FirePrimary.copy(alpha = 0.78f),                              // 3: Daily Goal Met
        FirePrimary                                                   // 4: Power Reading (2x goal / 45m+)
    )

    val effectiveGoal = dailyGoalMins.coerceAtLeast(5)

    // Build calendar-accurate weeks for the selected year and filter period
    val (weekColumns, monthStartColumnMap) = remember(
        dailyDataMap,
        selectedYear,
        selectedFilter,
        effectiveGoal,
        today
    ) {
        val firstDayOfPeriod = when (selectedFilter) {
            HeatmapYearFilter.FullYear -> LocalDate.of(selectedYear, 1, 1)
            HeatmapYearFilter.Q1 -> LocalDate.of(selectedYear, 1, 1)
            HeatmapYearFilter.Q2 -> LocalDate.of(selectedYear, 4, 1)
            HeatmapYearFilter.Q3 -> LocalDate.of(selectedYear, 7, 1)
            HeatmapYearFilter.Q4 -> LocalDate.of(selectedYear, 10, 1)
        }
        val lastDayOfPeriod = when (selectedFilter) {
            HeatmapYearFilter.FullYear -> LocalDate.of(selectedYear, 12, 31)
            HeatmapYearFilter.Q1 -> LocalDate.of(selectedYear, 3, 31)
            HeatmapYearFilter.Q2 -> LocalDate.of(selectedYear, 6, 30)
            HeatmapYearFilter.Q3 -> LocalDate.of(selectedYear, 9, 30)
            HeatmapYearFilter.Q4 -> LocalDate.of(selectedYear, 12, 31)
        }

        val gridStartMonday = firstDayOfPeriod.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val gridEndSunday = lastDayOfPeriod.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val totalDays = ChronoUnit.DAYS.between(gridStartMonday, gridEndSunday).toInt() + 1
        val totalWeeks = (totalDays / 7).coerceAtLeast(1)

        val monthColMap = mutableMapOf<Int, String>()
        val columns = List(totalWeeks) { weekIdx ->
            List(7) { dayIdx ->
                val cellDate = gridStartMonday.plusDays((weekIdx * 7 + dayIdx).toLong())
                val dStr = cellDate.format(formatter)
                val inYear = cellDate.year == selectedYear
                val inPeriod = inYear && cellDate.monthValue in selectedFilter.months

                if (inPeriod && cellDate.dayOfMonth == 1) {
                    monthColMap[weekIdx] = cellDate.month
                        .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        .uppercase()
                } else if (weekIdx == 0 && dayIdx == 0 && monthColMap[0] == null) {
                    monthColMap[0] = firstDayOfPeriod.month
                        .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        .uppercase()
                }

                val log = if (inPeriod) dailyDataMap[dStr] else null
                val mins = log?.minutesRead ?: 0
                val pages = log?.pagesRead ?: 0
                val isFuture = cellDate.isAfter(today)
                val isGoalMet = mins >= effectiveGoal

                val level = when {
                    !inPeriod || isFuture || mins <= 0 -> 0
                    mins >= maxOf(effectiveGoal * 2, 45) -> 4
                    mins >= effectiveGoal -> 3
                    mins >= (effectiveGoal / 2).coerceAtLeast(10) -> 2
                    else -> 1
                }

                YearlyHeatmapCell(
                    date = cellDate,
                    dateString = dStr,
                    isInTargetYear = inYear,
                    isInFilterPeriod = inPeriod,
                    isToday = cellDate == today,
                    isFuture = isFuture,
                    minutesRead = mins,
                    pagesRead = pages,
                    bookTitle = log?.bookTitle,
                    notes = log?.notes,
                    isGoalMet = isGoalMet,
                    intensityLevel = level
                )
            }
        }
        columns to monthColMap
    }

    // Auto-scroll to current week when viewing Full Year of current year
    LaunchedEffect(selectedYear, selectedFilter, weekColumns.size) {
        if (selectedYear == today.year && selectedFilter == HeatmapYearFilter.FullYear) {
            val todayWeekIdx = weekColumns.indexOfFirst { week ->
                week.any { it.date == today }
            }
            if (todayWeekIdx > 8) {
                val pxPerColumn = with(density) { 18.dp.toPx() }
                val targetScroll = ((todayWeekIdx - 10) * pxPerColumn).toInt().coerceAtLeast(0)
                scrollState.animateScrollTo(targetScroll)
            }
        } else {
            scrollState.scrollTo(0)
        }
    }

    // Calculate comprehensive yearly statistics
    val yearStats = remember(dailyDataMap, selectedYear, effectiveGoal, today) {
        val totalDaysInYear = Year.of(selectedYear).length()
        val elapsedDaysInYear = when {
            selectedYear < today.year -> totalDaysInYear
            selectedYear == today.year -> today.dayOfYear
            else -> 1
        }

        var activeDays = 0
        var goalMetDays = 0
        var totalMinutes = 0
        var totalPages = 0
        var longestStreakInYear = 0
        var runningStreak = 0
        val monthlyActiveDays = IntArray(12)
        val monthlyMinutes = IntArray(12)

        val startOfYear = LocalDate.of(selectedYear, 1, 1)
        for (i in 0 until totalDaysInYear) {
            val date = startOfYear.plusDays(i.toLong())
            val dStr = date.format(formatter)
            val log = dailyDataMap[dStr]
            val mins = log?.minutesRead ?: 0
            val pgs = log?.pagesRead ?: 0

            if (mins > 0 && !date.isAfter(today)) {
                activeDays++
                totalMinutes += mins
                totalPages += if (pgs > 0) pgs else ((mins * 12) / 10).coerceAtLeast(1)
                monthlyActiveDays[date.monthValue - 1]++
                monthlyMinutes[date.monthValue - 1] += mins
            }

            if (mins >= effectiveGoal && !date.isAfter(today)) {
                goalMetDays++
                runningStreak++
                if (runningStreak > longestStreakInYear) {
                    longestStreakInYear = runningStreak
                }
            } else if (!date.isAfter(today)) {
                runningStreak = 0
            }
        }

        val consistencyPct = if (elapsedDaysInYear > 0) {
            ((activeDays * 100) / elapsedDaysInYear).coerceIn(0, 100)
        } else 0

        YearlyStatsSummary(
            activeDays = activeDays,
            goalMetDays = goalMetDays,
            totalMinutes = totalMinutes,
            totalPages = totalPages,
            longestStreak = longestStreakInYear,
            consistencyPercent = consistencyPct,
            totalDaysInYear = totalDaysInYear,
            monthlyActiveDays = monthlyActiveDays.toList(),
            monthlyMinutes = monthlyMinutes.toList()
        )
    }

    val totalTimeFormatted = remember(yearStats.totalMinutes) {
        val mins = yearStats.totalMinutes
        if (mins >= 60) {
            val h = mins / 60
            val m = mins % 60
            if (m == 0) "${h}h" else "${h}h ${m}m"
        } else {
            "${mins}m"
        }
    }

    // Selected date detail lookup
    val selectedDateStr = remember(selectedDate) { selectedDate.format(formatter) }
    val selectedDayLog = remember(selectedDateStr, dailyDataMap) { dailyDataMap[selectedDateStr] }
    val selectedDaySessions = remember(selectedDateStr, sessionsByDate) {
        sessionsByDate[selectedDateStr].orEmpty()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("yearly_activity_heatmap_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .testTag("stats_consistency_grid")
        ) {
            // ==========================================
            // 1. HEADER & YEAR SWITCHER
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(FirePrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Yearly Activity Heatmap",
                            tint = FirePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Yearly Activity Heatmap",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${yearStats.activeDays} active days • ${yearStats.goalMetDays} goals met in $selectedYear",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Year Navigation (< 2026 >)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = {
                            selectedYear -= 1
                            selectedDate = LocalDate.of(selectedYear, 1, 1)
                        },
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("yearly_heatmap_prev_year")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Year",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "$selectedYear",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        ),
                        color = FirePrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = {
                            if (selectedYear < today.year) {
                                selectedYear += 1
                                selectedDate = if (selectedYear == today.year) today else LocalDate.of(selectedYear, 1, 1)
                            }
                        },
                        enabled = selectedYear < today.year,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("yearly_heatmap_next_year")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Year",
                            tint = if (selectedYear < today.year) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 2. YEARLY SUMMARY STATS STRIP
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f))
                    .padding(vertical = 10.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                YearlyStatMiniColumn(
                    label = "ACTIVE DAYS",
                    value = "${yearStats.activeDays}d",
                    subtext = "${yearStats.consistencyPercent}% rate",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(OutlineVariant.copy(alpha = 0.5f))
                )

                YearlyStatMiniColumn(
                    label = "GOALS MET",
                    value = "${yearStats.goalMetDays}d",
                    subtext = "${effectiveGoal}m+ daily",
                    valueColor = SuccessGreen
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(OutlineVariant.copy(alpha = 0.5f))
                )

                YearlyStatMiniColumn(
                    label = "TOTAL TIME",
                    value = totalTimeFormatted,
                    subtext = "${yearStats.totalPages} pgs",
                    valueColor = FirePrimary
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(OutlineVariant.copy(alpha = 0.5f))
                )

                YearlyStatMiniColumn(
                    label = "YEAR STREAK",
                    value = "${yearStats.longestStreak}d",
                    subtext = "Best run",
                    valueColor = FirePrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 3. QUARTER / FULL YEAR FILTER CHIPS
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeatmapYearFilter.values().forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) FirePrimary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .clickable { selectedFilter = filter }
                            .testTag("yearly_heatmap_filter_${filter.name.lowercase()}")
                    ) {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                if (selectedYear != today.year || selectedDate != today) {
                    TextButton(
                        onClick = {
                            selectedYear = today.year
                            selectedFilter = HeatmapYearFilter.FullYear
                            selectedDate = today
                        },
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = "Jump to Today",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = FirePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 4. 53-WEEK x 7-DAY HEATMAP MATRIX
            // ==========================================
            Row(modifier = Modifier.fillMaxWidth()) {
                // Fixed Left Column: Day-of-Week Labels (Mon - Sun)
                Column(
                    modifier = Modifier
                        .padding(top = 20.dp, end = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val dayLabels = listOf("Mon", "", "Wed", "", "Fri", "", "Sun")
                    dayLabels.forEach { label ->
                        Box(
                            modifier = Modifier.height(14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Horizontally Scrollable Week Columns + Month Labels
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                ) {
                    // Month Header Row aligned with exact week columns
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        var lastLabelCol = -4
                        weekColumns.indices.forEach { colIdx ->
                            val monthName = monthStartColumnMap[colIdx]
                            val showLabel = monthName != null && (colIdx - lastLabelCol >= 3 || colIdx == 0)
                            if (showLabel) {
                                lastLabelCol = colIdx
                            }
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (showLabel && monthName != null) {
                                    Text(
                                        text = monthName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        softWrap = false,
                                        overflow = TextOverflow.Visible
                                    )
                                }
                            }
                        }
                    }

                    // 7 Rows x N Weeks Matrix
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        weekColumns.forEach { weekCells ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                weekCells.forEach { cell ->
                                    if (!cell.isInFilterPeriod) {
                                        // Out-of-year / out-of-quarter padding day
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(RoundedCornerShape(3.5.dp))
                                                .background(Color.Transparent)
                                        )
                                    } else {
                                        val isSelected = cell.date == selectedDate
                                        val targetBg = when {
                                            cell.isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
                                            else -> heatColors[cell.intensityLevel]
                                        }
                                        val animatedBg by animateColorAsState(
                                            targetValue = targetBg,
                                            animationSpec = tween(180),
                                            label = "yearly_cell_bg"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(RoundedCornerShape(3.5.dp))
                                                .background(animatedBg)
                                                .then(
                                                    if (isSelected) {
                                                        Modifier.border(
                                                            width = 1.8.dp,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            shape = RoundedCornerShape(3.5.dp)
                                                        )
                                                    } else if (cell.isToday) {
                                                        Modifier.border(
                                                            width = 1.4.dp,
                                                            color = FirePrimaryContainer,
                                                            shape = RoundedCornerShape(3.5.dp)
                                                        )
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                                .clickable(enabled = !cell.isFuture) {
                                                    selectedDate = cell.date
                                                    onDayClick(cell.dateString, cell.minutesRead)
                                                }
                                                .testTag("yearly_heatmap_cell_${cell.dateString}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Subtle white center dot when daily goal is met on high intensity
                                            if (cell.isGoalMet && !cell.isFuture) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.5.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (cell.intensityLevel >= 3) Color.White.copy(alpha = 0.85f)
                                                            else FirePrimary
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 5. 12-MONTH MINI DISTRIBUTION SPARK-BARS
            // ==========================================
            val shortMonths = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Activity Breakdown ($selectedYear)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap month to focus",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    shortMonths.forEachIndexed { index, monthInitial ->
                        val activeInMonth = yearStats.monthlyActiveDays[index]
                        val maxDaysInMonth = Month.of(index + 1).length(Year.isLeap(selectedYear.toLong()))
                        val fillFraction = (activeInMonth.toFloat() / maxDaysInMonth.toFloat()).coerceIn(0f, 1f)
                        val isCurrentSelectedMonth = selectedDate.year == selectedYear && selectedDate.monthValue == (index + 1)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    val targetMonthDate = LocalDate.of(selectedYear, index + 1, 1)
                                    selectedDate = if (selectedYear == today.year && index + 1 == today.monthValue) {
                                        today
                                    } else {
                                        targetMonthDate
                                    }
                                    if (selectedFilter != HeatmapYearFilter.FullYear) {
                                        selectedFilter = HeatmapYearFilter.FullYear
                                    }
                                    // Scroll to the selected month column
                                    val targetWeekIdx = weekColumns.indexOfFirst { week ->
                                        week.any { it.isInTargetYear && it.date.monthValue == index + 1 }
                                    }
                                    if (targetWeekIdx >= 0) {
                                        coroutineScope.launch {
                                            val pxPerCol = with(density) { 18.dp.toPx() }
                                            scrollState.animateScrollTo((targetWeekIdx * pxPerCol).toInt())
                                        }
                                    }
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = if (activeInMonth > 0) "$activeInMonth" else "",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentSelectedMonth) FirePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                if (fillFraction > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(fillFraction.coerceAtLeast(0.15f))
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (isCurrentSelectedMonth) FirePrimary
                                                else FirePrimaryContainer.copy(alpha = 0.8f)
                                            )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = monthInitial,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = if (isCurrentSelectedMonth) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isCurrentSelectedMonth) FirePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 6. HEATMAP INTENSITY LEGEND
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(FirePrimary)
                    )
                    Text(
                        text = "Dot = ${effectiveGoal}m+ daily goal met",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Less",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    heatColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(RoundedCornerShape(2.5.dp))
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "More",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // 7. INTERACTIVE SELECTED DAY INSPECTION CARD
            // ==========================================
            YearlySelectedDayCard(
                date = selectedDate,
                log = selectedDayLog,
                daySessions = selectedDaySessions,
                dailyGoalMins = effectiveGoal
            )
        }
    }
}

private data class YearlyStatsSummary(
    val activeDays: Int,
    val goalMetDays: Int,
    val totalMinutes: Int,
    val totalPages: Int,
    val longestStreak: Int,
    val consistencyPercent: Int,
    val totalDaysInYear: Int,
    val monthlyActiveDays: List<Int>,
    val monthlyMinutes: List<Int>
)

@Composable
private fun YearlyStatMiniColumn(
    label: String,
    value: String,
    subtext: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.4.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
        Text(
            text = subtext,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun YearlySelectedDayCard(
    date: LocalDate,
    log: ReadingLog?,
    daySessions: List<ReadingSession>,
    dailyGoalMins: Int
) {
    val today = remember { LocalDate.now() }
    val isToday = date == today
    val minutesRead = log?.minutesRead ?: 0
    val pagesRead = log?.pagesRead ?: 0
    val isGoalMet = minutesRead >= dailyGoalMins

    val formattedDate = remember(date) {
        val dow = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        "$dow, $month ${date.dayOfMonth}, ${date.year}"
    }

    val booksReadText = remember(log, daySessions) {
        val titles = daySessions.map { it.bookTitle }.filter { it.isNotBlank() }.distinct()
        when {
            titles.isNotEmpty() -> titles.joinToString(", ")
            !log?.bookTitle.isNullOrBlank() -> log?.bookTitle ?: ""
            else -> ""
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (minutesRead > 0) FirePrimary.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
            .border(
                width = 1.dp,
                color = if (minutesRead > 0) FirePrimary.copy(alpha = 0.25f) else OutlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
            .testTag("yearly_heatmap_selected_day_card"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (minutesRead > 0) FirePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = formattedDate,
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
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "TODAY",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

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
                        text = "$minutesRead min" + if (pagesRead > 0) " • $pagesRead pages" else "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = FirePrimary
                    )
                    if (booksReadText.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = booksReadText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Text(
                    text = "Rest day • Tap any colored square to inspect reading activity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Status Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when {
                        isGoalMet -> SuccessGreen.copy(alpha = 0.15f)
                        minutesRead > 0 -> FirePrimary.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                )
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (minutesRead > 0) {
                    Icon(
                        imageVector = if (isGoalMet) Icons.Default.CheckCircle else Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (isGoalMet) SuccessGreen else FirePrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = when {
                        isGoalMet -> "Goal Met"
                        minutesRead > 0 -> "Logged"
                        else -> "0 min"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isGoalMet -> SuccessGreen
                        minutesRead > 0 -> FirePrimary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
