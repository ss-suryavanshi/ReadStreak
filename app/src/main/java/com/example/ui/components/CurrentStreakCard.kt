package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReadingLog
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.FirePrimaryContainer
import com.example.ui.theme.OnSuccessGreenContainer
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Data model representing the calculated consecutive reading goal streak state.
 */
data class GoalStreakCalculation(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalGoalMetDays: Int,
    val totalActiveDays: Int,
    val isTodayGoalMet: Boolean,
    val todayMinutesRead: Int,
    val dailyGoalMinutes: Int,
    val isStreakAtRisk: Boolean,
    val streakStartDate: LocalDate?,
    val streakEndDate: LocalDate?,
    val recentSevenDays: List<DayGoalStatus>
)

data class DayGoalStatus(
    val date: LocalDate,
    val label: String,
    val minutesRead: Int,
    val isGoalMet: Boolean,
    val isToday: Boolean
)

/**
 * Pure calculation utility that computes consecutive days the user has met their reading goal.
 */
object StreakCalculator {
    private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    fun calculateGoalStreak(
        logs: List<ReadingLog>,
        dailyGoalMins: Int,
        today: LocalDate = LocalDate.now()
    ): GoalStreakCalculation {
        val effectiveGoal = dailyGoalMins.coerceAtLeast(1)

        // Aggregate minutes per date
        val minutesByDate = mutableMapOf<LocalDate, Int>()
        for (log in logs) {
            try {
                val parsedDate = LocalDate.parse(log.dateString, DATE_FORMATTER)
                minutesByDate[parsedDate] = (minutesByDate[parsedDate] ?: 0) + log.minutesRead
            } catch (_: Exception) {
                // Ignore malformed date strings
            }
        }

        // Set of dates where the user met or exceeded their daily reading goal
        val goalMetDates = minutesByDate
            .filterValues { it >= effectiveGoal }
            .keys
            .sorted()
        val goalMetSet = goalMetDates.toSet()

        val todayMinutes = minutesByDate[today] ?: 0
        val isTodayGoalMet = todayMinutes >= effectiveGoal
        val yesterday = today.minusDays(1)
        val isYesterdayGoalMet = goalMetSet.contains(yesterday)

        // Calculate consecutive days meeting the reading goal
        var currentStreak = 0
        var checkDate: LocalDate? = when {
            isTodayGoalMet -> today
            isYesterdayGoalMet -> yesterday
            else -> null
        }

        val streakEndDate = checkDate
        var streakStartDate: LocalDate? = null

        while (checkDate != null && goalMetSet.contains(checkDate)) {
            currentStreak++
            streakStartDate = checkDate
            checkDate = checkDate.minusDays(1)
        }

        // Calculate all-time best streak of consecutive goal-met days
        var bestStreak = 0
        var runningStreak = 0
        var previousDate: LocalDate? = null

        for (date in goalMetDates) {
            if (previousDate == null) {
                runningStreak = 1
            } else if (date == previousDate.plusDays(1)) {
                runningStreak++
            } else if (date != previousDate) {
                runningStreak = 1
            }
            if (runningStreak > bestStreak) {
                bestStreak = runningStreak
            }
            previousDate = date
        }

        bestStreak = maxOf(bestStreak, currentStreak)

        // Build the last 7 days status list ending with today
        val recentSevenDays = (6 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong())
            val mins = minutesByDate[d] ?: 0
            val label = if (offset == 0) {
                "TODAY"
            } else {
                d.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
            }
            DayGoalStatus(
                date = d,
                label = label,
                minutesRead = mins,
                isGoalMet = mins >= effectiveGoal,
                isToday = offset == 0
            )
        }

        return GoalStreakCalculation(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalGoalMetDays = goalMetDates.size,
            totalActiveDays = minutesByDate.count { it.value > 0 },
            isTodayGoalMet = isTodayGoalMet,
            todayMinutesRead = todayMinutes,
            dailyGoalMinutes = effectiveGoal,
            isStreakAtRisk = currentStreak > 0 && !isTodayGoalMet,
            streakStartDate = streakStartDate,
            streakEndDate = streakEndDate,
            recentSevenDays = recentSevenDays
        )
    }
}

/**
 * Prominent 'Current Streak' display card that calculates and shows the number of
 * consecutive days the user has met their reading goal.
 */
@Composable
fun CurrentStreakCard(
    logs: List<ReadingLog>,
    dailyGoalMins: Int = 20,
    modifier: Modifier = Modifier
) {
    val streakData = remember(logs, dailyGoalMins) {
        StreakCalculator.calculateGoalStreak(logs = logs, dailyGoalMins = dailyGoalMins)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "current_streak_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }
    val streakRangeLabel = remember(streakData.streakStartDate, streakData.streakEndDate, streakData.currentStreak) {
        val start = streakData.streakStartDate
        val end = streakData.streakEndDate
        if (streakData.currentStreak > 0 && start != null && end != null) {
            if (start == end) {
                "Started ${start.format(dateFormatter)}"
            } else {
                "${start.format(dateFormatter)} – ${end.format(dateFormatter)}"
            }
        } else {
            "Read $dailyGoalMins mins today to start"
        }
    }

    // Milestone calculation
    val milestones = listOf(3, 7, 14, 21, 30, 50, 75, 100, 150, 200, 365)
    val nextMilestone = milestones.firstOrNull { it > streakData.currentStreak } ?: (streakData.currentStreak + 30)
    val prevMilestone = milestones.lastOrNull { it <= streakData.currentStreak } ?: 0
    val milestoneProgress = if (nextMilestone > prevMilestone) {
        ((streakData.currentStreak - prevMilestone).toFloat() / (nextMilestone - prevMilestone).toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedMilestoneProgress by animateFloatAsState(
        targetValue = milestoneProgress,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "milestone_progress"
    )

    val todayGoalProgress = (streakData.todayMinutesRead.toFloat() / streakData.dailyGoalMinutes.toFloat()).coerceIn(0f, 1f)
    val animatedTodayProgress by animateFloatAsState(
        targetValue = todayGoalProgress,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "today_goal_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("current_streak_display_card"),
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
            // 1. TOP STATUS ROW (CURRENT STREAK LABEL + STATUS PILL)
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
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FirePrimary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Current Streak Flame",
                            tint = FirePrimaryContainer,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(if (streakData.currentStreak > 0) flameScale else 1f)
                        )
                    }

                    Column {
                        Text(
                            text = "CURRENT STREAK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = FirePrimary
                        )
                        Text(
                            text = "Daily Goal: ${streakData.dailyGoalMinutes} mins/day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                val badgeBg = when {
                    streakData.isTodayGoalMet -> SuccessGreen.copy(alpha = 0.15f)
                    streakData.isStreakAtRisk -> Color(0xFFFF9800).copy(alpha = 0.18f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val badgeTint = when {
                    streakData.isTodayGoalMet -> SuccessGreen
                    streakData.isStreakAtRisk -> Color(0xFFE65100)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val badgeText = when {
                    streakData.isTodayGoalMet -> "Goal Met Today"
                    streakData.isStreakAtRisk -> "Pending Today"
                    else -> "No Active Streak"
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                streakData.isTodayGoalMet -> Icons.Default.CheckCircle
                                streakData.isStreakAtRisk -> Icons.Default.Schedule
                                else -> Icons.Default.LocalFireDepartment
                            },
                            contentDescription = null,
                            tint = badgeTint,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = badgeTint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. HERO CONSECUTIVE DAYS COUNTER & BEST STREAK COMPARISON
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                FirePrimary.copy(alpha = 0.12f),
                                FirePrimaryContainer.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${streakData.currentStreak}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        ),
                        color = FirePrimaryContainer,
                        modifier = Modifier.testTag("current_streak_count_text")
                    )
                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                        Text(
                            text = if (streakData.currentStreak == 1) "Consecutive Day" else "Consecutive Days",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = streakRangeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Personal Best Pill
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Best: ${streakData.bestStreak}d",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${streakData.totalGoalMetDays} goal days total",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. TODAY'S GOAL PROGRESS TOWARDS STREAK
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (streakData.isTodayGoalMet) {
                            "Today's reading goal met! Streak secured 🔥"
                        } else {
                            val minsLeft = (streakData.dailyGoalMinutes - streakData.todayMinutesRead).coerceAtLeast(0)
                            "$minsLeft mins left today to extend your streak"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (streakData.isTodayGoalMet) SuccessGreen else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${streakData.todayMinutesRead} / ${streakData.dailyGoalMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (streakData.isTodayGoalMet) SuccessGreen else FirePrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { animatedTodayProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (streakData.isTodayGoalMet) SuccessGreen else FirePrimaryContainer,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. PAST 7 CONSECUTIVE DAYS GOAL TRACKER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                streakData.recentSevenDays.forEach { dayStatus ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (dayStatus.isToday) 38.dp else 34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        dayStatus.isGoalMet -> SuccessGreenContainer
                                        dayStatus.minutesRead > 0 -> FirePrimary.copy(alpha = 0.18f)
                                        dayStatus.isToday -> MaterialTheme.colorScheme.surface
                                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                    }
                                )
                                .border(
                                    width = if (dayStatus.isToday) 2.dp else 1.dp,
                                    color = when {
                                        dayStatus.isGoalMet -> Color.Transparent
                                        dayStatus.isToday -> FirePrimaryContainer
                                        dayStatus.minutesRead > 0 -> FirePrimary.copy(alpha = 0.5f)
                                        else -> OutlineVariant.copy(alpha = 0.5f)
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                dayStatus.isGoalMet -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Goal met",
                                        tint = OnSuccessGreenContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                dayStatus.minutesRead > 0 -> {
                                    Text(
                                        text = "${dayStatus.minutesRead}m",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = FirePrimary
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "${dayStatus.date.dayOfMonth}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = if (dayStatus.isToday) FontWeight.Bold else FontWeight.Medium,
                                        color = if (dayStatus.isToday) FirePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = dayStatus.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = if (dayStatus.isToday) 9.sp else 10.sp
                            ),
                            fontWeight = if (dayStatus.isToday) FontWeight.Black else FontWeight.SemiBold,
                            color = if (dayStatus.isToday) FirePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. NEXT MILESTONE PROGRESS FOOTER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Next Milestone: $nextMilestone-Day Streak",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${(nextMilestone - streakData.currentStreak).coerceAtLeast(1)}d to go (${(animatedMilestoneProgress * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold,
                    color = FirePrimary
                )
            }
        }
    }
}
