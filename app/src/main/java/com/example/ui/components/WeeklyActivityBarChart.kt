package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReadingLog
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.FirePrimaryContainer
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.SuccessGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class DailyActivityData(
    val date: LocalDate,
    val dayLabel: String,
    val formattedDate: String,
    val minutesRead: Int,
    val isToday: Boolean,
    val isGoalMet: Boolean
)

@Composable
fun WeeklyActivityBarChart(
    logs: List<ReadingLog>,
    dailyGoalMins: Int = 20,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    // Map logs by date
    val logMap = remember(logs) { logs.associate { it.dateString to it.minutesRead } }

    // Generate past 7 days (including today)
    val past7Days = remember(logs, today) {
        (6 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong())
            val dateStr = d.format(formatter)
            val mins = logMap[dateStr] ?: 0
            val dayLabel = d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val formattedDate = d.format(DateTimeFormatter.ofPattern("MMM d"))

            DailyActivityData(
                date = d,
                dayLabel = dayLabel,
                formattedDate = formattedDate,
                minutesRead = mins,
                isToday = offset == 0,
                isGoalMet = mins >= dailyGoalMins
            )
        }
    }

    val maxMins = remember(past7Days, dailyGoalMins) {
        val peak = past7Days.maxOfOrNull { it.minutesRead } ?: 0
        (maxOf(peak, dailyGoalMins) * 1.25f).coerceAtLeast(40f).toInt()
    }

    val totalWeeklyMins = remember(past7Days) { past7Days.sumOf { it.minutesRead } }
    val avgDailyMins = remember(totalWeeklyMins) { totalWeeklyMins / 7 }
    val daysGoalMetCount = remember(past7Days) { past7Days.count { it.isGoalMet } }
    val topDay = remember(past7Days) { past7Days.maxByOrNull { it.minutesRead } }

    var selectedDayIndex by remember { mutableStateOf<Int?>(past7Days.indexOfLast { it.isToday }.takeIf { it >= 0 }) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(logs) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, OutlineVariant, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("weekly_activity_chart_container")
    ) {
        // Chart Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Activity Chart",
                        tint = FirePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Weekly Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Reading patterns past 7 days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Summary Avg Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FirePrimaryContainer.copy(alpha = 0.25f),
                border = androidx.compose.foundation.BorderStroke(1.dp, FirePrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = FirePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${avgDailyMins}m / day avg",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FirePrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selected Day Interactive Tooltip Popup (Recharts Hover/Tap style)
        val selectedDay = selectedDayIndex?.let { past7Days.getOrNull(it) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selectedDay != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${selectedDay.dayLabel} (${selectedDay.formattedDate}):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${selectedDay.minutesRead} mins",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedDay.isGoalMet) FirePrimary else MaterialTheme.colorScheme.onSurface
                        )
                        if (selectedDay.isGoalMet) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Goal Met",
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Tap a bar to view daily details",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Recharts-Style Bar Chart Canvas + Overlay
        val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        val goalLineColor = FirePrimary.copy(alpha = 0.65f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            // Background Grid Lines & Daily Goal Reference Line
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Y-Axis Grid steps (0, 50%, 100%)
                val steps = listOf(0.2f, 0.5f, 0.8f)
                steps.forEach { ratio ->
                    val y = canvasHeight * ratio
                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Goal Target Line
                val goalRatio = 1f - (dailyGoalMins.toFloat() / maxMins).coerceIn(0f, 1f)
                val goalY = canvasHeight * goalRatio
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                drawLine(
                    color = goalLineColor,
                    start = Offset(0f, goalY),
                    end = Offset(canvasWidth, goalY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = pathEffect
                )
            }

            // Bars Row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                past7Days.forEachIndexed { index, dayData ->
                    val isSelected = selectedDayIndex == index
                    val heightRatio = (dayData.minutesRead.toFloat() / maxMins).coerceIn(0.04f, 1f)
                    val animatedHeightFraction = heightRatio * animationProgress.value

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedDayIndex = if (selectedDayIndex == index) null else index
                            }
                            .testTag("bar_chart_day_$index")
                    ) {
                        // Top Minute Label on active bars
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (dayData.minutesRead > 0 && isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = FirePrimary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "${dayData.minutesRead}m",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Bar Container
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 28.dp else 22.dp)
                                .fillMaxHeight(fraction = animatedHeightFraction)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(
                                    when {
                                        dayData.minutesRead == 0 -> Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                        )
                                        dayData.isGoalMet -> Brush.verticalGradient(
                                            listOf(
                                                Color(0xFFFF6B35),
                                                Color(0xFFAB3500)
                                            )
                                        )
                                        else -> Brush.verticalGradient(
                                            listOf(
                                                Color(0xFFFFB59D),
                                                Color(0xFFFF8A65)
                                            )
                                        )
                                    }
                                )
                                .then(
                                    if (isSelected) Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                    ) else Modifier
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // X-Axis Day Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            past7Days.forEachIndexed { index, dayData ->
                val isSelected = selectedDayIndex == index

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = dayData.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (dayData.isToday || isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = when {
                            isSelected -> FirePrimary
                            dayData.isToday -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    if (dayData.isToday) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(FirePrimary)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Stats Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = FirePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$daysGoalMetCount/7 days goal met (${dailyGoalMins}m/d)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                topDay?.let { top ->
                    if (top.minutesRead > 0) {
                        Text(
                            text = "Peak: ${top.dayLabel} (${top.minutesRead}m)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }
    }
}
