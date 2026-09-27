package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Book
import com.example.data.ReadingLog
import com.example.data.ReadingSession
import com.example.data.UserEntity
import com.example.timer.ReadingTimerManager
import com.example.timer.ReadingTimerState
import com.example.ui.components.CelebrationConfetti
import com.example.ui.components.ReadingSessionsSection
import com.example.ui.components.YearlyActivityHeatmap
import com.example.ui.components.ads.InterstitialAdDialog
import com.example.ui.components.ads.NativeAdBannerCard
import com.example.ui.components.ads.RewardedAdDialog
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.FirePrimaryContainer
import com.example.ui.theme.OnSuccessGreenContainer
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.SuccessGreenContainer
import kotlinx.coroutines.delay

@Composable
fun StreakHomeScreen(
    currentUser: UserEntity? = null,
    currentStreak: Int,
    bestStreak: Int,
    completedDays: Int,
    yearlyGoal: Int,
    weekDays: List<Boolean>,
    isTodayRead: Boolean,
    timerState: ReadingTimerState = ReadingTimerState(),
    dailyGoalMins: Int = 20,
    userLevel: Int = 7,
    userTitle: String = "Flamekeeper",
    currentXp: Int = 750,
    xpForNextLevel: Int = 1000,
    gemsCount: Int = 340,
    isBoostActive: Boolean = true,
    boostMultiplier: String = "1.5x",
    todayMinutesRead: Int = 0,
    isDailyQuestClaimed: Boolean = false,
    logs: List<ReadingLog> = emptyList(),
    sessions: List<ReadingSession> = emptyList(),
    books: List<Book> = emptyList(),
    onClaimDailyQuest: () -> Unit = {},
    onLogCompletedMinutes: (Int, String) -> Unit = { _, _ -> },
    onLogSession: (String, Int, Int, String) -> Unit = { _, _, _, _ -> },
    onDeleteSession: (Int) -> Unit = {},
    onLogTodayRead: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWidgetPreviews: () -> Unit,
    onOpenAuth: () -> Unit = {},
    onOpenInsights: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Pulsing animation for active elements
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    // Calculate next milestone
    val milestones = listOf(3, 7, 14, 21, 30, 50, 75, 100, 150, 200, 365)
    val nextMilestone = milestones.firstOrNull { it > currentStreak } ?: (currentStreak + 50)
    val prevMilestone = milestones.lastOrNull { it <= currentStreak } ?: 0
    val daysRemaining = (nextMilestone - currentStreak).coerceAtLeast(1)
    val milestoneProgress = if (nextMilestone > prevMilestone) {
        ((currentStreak - prevMilestone).toFloat() / (nextMilestone - prevMilestone).toFloat()).coerceIn(0f, 1f)
    } else 0f

    val todayIndex = remember { (java.time.LocalDate.now().dayOfWeek.value - 1).coerceIn(0, 6) }
    val baseDayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val dayLabels = remember(todayIndex) {
        baseDayLabels.mapIndexed { idx, lbl -> if (idx == todayIndex) "TODAY" else lbl }
    }

    // Ad dialog states
    var showInterstitialAd by remember { mutableStateOf(false) }
    var showRewardedAd by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    // Focus Sprint preset selected
    var selectedSprintMinutes by remember { mutableIntStateOf(20) }

    if (showInterstitialAd) {
        InterstitialAdDialog(
            adTitle = "Kindle Unlimited: Read Anywhere",
            adDescription = "Explore millions of eBooks, magazines, and audiobooks on your phone or tablet.",
            ctaUrl = "https://www.amazon.com/kindle-dbs/hz/signup",
            onAdDismissed = {
                showInterstitialAd = false
                showConfetti = true
                onLogTodayRead()
            }
        )
    }

    if (showRewardedAd) {
        RewardedAdDialog(
            rewardTitle = "+50 Bonus Flame Points",
            rewardDescription = "Watch a short 5-second sponsor video to claim bonus streak protection points!",
            onRewardEarned = {
                Toast.makeText(context, "Bonus Flame Points Earned! 💎 +25", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showRewardedAd = false }
        )
    }

    LaunchedEffect(showConfetti) {
        if (showConfetti) {
            delay(3000L)
            showConfetti = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ==========================================
                // 1. TOP APP BAR (Logo, Lvl 7 Flamekeeper, Boost, Gems, Settings)
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo + App Name + Subtitle Level
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.read_streak_logo_1785676270425),
                            contentDescription = "Read Streak Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Column {
                            Text(
                                text = "Read Streak",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = FirePrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF9500))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Lvl $userLevel $userTitle",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Right Badges (1.5x Boost, Gems, Settings)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1.5x Boost Badge
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFF3E0),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.8f)),
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "$boostMultiplier Boost Active! Logs earn 50% more XP", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Boost",
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "$boostMultiplier Boost",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }

                        // Diamonds / Gems Badge
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFE0F7FA),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80DEEA).copy(alpha = 0.8f)),
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "You have $gemsCount gems! Complete quests to earn more.", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Diamond,
                                    contentDescription = "Gems",
                                    tint = Color(0xFF00838F),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "$gemsCount",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF006064)
                                )
                            }
                        }

                        // Settings Icon Button
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // 2. XP BAR & LEVEL CARD
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FirePrimaryContainer,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "LVL $userLevel",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = userTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "$currentXp / $xpForNextLevel XP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = FirePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Gradient Progress Bar
                        val xpProgress = (currentXp.toFloat() / xpForNextLevel.toFloat()).coerceIn(0f, 1f)
                        val animatedXp by animateFloatAsState(
                            targetValue = xpProgress,
                            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                            label = "xp_progress"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedXp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFFF9500),
                                                FirePrimaryContainer,
                                                Color(0xFFFF3D00)
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "1.5x XP Boost Active",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Text(
                                text = "${xpForNextLevel - currentXp} XP to Level ${userLevel + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 3. STREAK BANNER WITH "ACTIVE" TAG
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Active status badge with pulsing dot
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE8F5E9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .scale(dotAlpha)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E7D32))
                                    )
                                    Text(
                                        text = "ACTIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "READING STREAK",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isTodayRead) "Great job! Flame secured for today" else "Keep the flame burning! Read today",
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF9500),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 4. HERO STREAK SECTION WITH FLAME & DYNAMIC MILESTONE
                // ==========================================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        // Ambient glowing backdrop circle
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF9500).copy(alpha = 0.25f),
                                            Color(0xFFFF6B35).copy(alpha = 0.08f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentStreak.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 90.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-2).sp
                                ),
                                color = FirePrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Fire icon",
                                tint = Color(0xFFFF9500),
                                modifier = Modifier
                                    .size(54.dp)
                                    .scale(if (!isTodayRead) pulseScale else 1f)
                            )
                        }
                    }

                    Text(
                        text = "CURRENT STREAK • DAYS GOAL MET",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currentStreak consecutive ${if (currentStreak == 1) "day" else "days"} meeting your ${dailyGoalMins}m goal • Best: ${bestStreak}d",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = FirePrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic Milestone Card / Pill
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clickable { onOpenWidgetPreviews() }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "Milestone",
                                        tint = Color(0xFFFF9500),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Next Milestone: $nextMilestone Days",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "$daysRemaining days left",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = FirePrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Mini progress bar towards next milestone
                            LinearProgressIndicator(
                                progress = { milestoneProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFFF9500),
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 5. 7-DAY WEEK ROW (Circle view of streak days)
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dayLabels.forEachIndexed { index, label ->
                        val isChecked = weekDays.getOrElse(index) { false }
                        val isToday = label == "TODAY"

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isToday) 48.dp else 40.dp)
                                    .then(if (isToday && !isChecked) Modifier.scale(pulseScale) else Modifier)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isChecked -> SuccessGreenContainer
                                            isToday -> MaterialTheme.colorScheme.background
                                            else -> MaterialTheme.colorScheme.surfaceContainerLow
                                        }
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = when {
                                            isChecked -> Color.Transparent
                                            isToday -> FirePrimaryContainer
                                            else -> OutlineVariant.copy(alpha = 0.6f)
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = OnSuccessGreenContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (isToday) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(FirePrimaryContainer)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = if (isToday) 11.sp else 10.sp
                                ),
                                color = if (isToday) FirePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isChecked) 1f else 0.5f),
                                fontWeight = if (isToday) FontWeight.Black else FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 6. MAIN CTA BUTTON ("I read today")
                // ==========================================
                Button(
                    onClick = {
                        if (!isTodayRead) {
                            showInterstitialAd = true
                        } else {
                            Toast.makeText(context, "Already logged for today! Keep up the great reading habit.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .testTag("log_today_reading_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTodayRead) SuccessGreenContainer else FirePrimaryContainer,
                        contentColor = if (isTodayRead) OnSuccessGreenContainer else Color.White
                    ),
                    shape = RoundedCornerShape(30.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isTodayRead) "Completed for Today! 🎉" else "I read today",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isTodayRead) Icons.Default.Check else Icons.Default.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 7. DAILY QUEST CARD ("Read for 20 minutes")
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🎯 DAILY QUEST",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = FirePrimary
                                )
                            }
                            Text(
                                text = "Resets at midnight",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Read for $dailyGoalMins minutes",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Daily quest progress bar
                        val questTarget = dailyGoalMins.coerceAtLeast(1)
                        val questProgress = (todayMinutesRead.toFloat() / questTarget.toFloat()).coerceIn(0f, 1f)
                        val isQuestReadyToClaim = todayMinutesRead >= questTarget && !isDailyQuestClaimed

                        LinearProgressIndicator(
                            progress = { questProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (questProgress >= 1f) Color(0xFF2E7D32) else FirePrimaryContainer,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$todayMinutesRead / $dailyGoalMins min",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFF3E0),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFFB74D))
                                ) {
                                    Text(
                                        text = "+50 XP • +10 💎",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Quest Action Button
                            if (isDailyQuestClaimed) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Claimed ✓",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            } else if (isQuestReadyToClaim) {
                                Button(
                                    onClick = {
                                        showConfetti = true
                                        onClaimDailyQuest()
                                        Toast.makeText(context, "Quest Claimed! +50 XP and +10 Gems! 💎", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2E7D32)
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = "Claim Reward",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = FirePrimaryContainer.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable {
                                        ReadingTimerManager.startTimer(context, dailyGoalMins, "Daily Reading Quest")
                                        Toast.makeText(context, "Timer started for $dailyGoalMins minutes!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = FirePrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Start Quest",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = FirePrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 8. FOCUS SPRINT TIMER CARD
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                                        .clip(CircleShape)
                                        .background(FirePrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Timer",
                                        tint = FirePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Focus Sprint Timer",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Read distraction-free & earn XP",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (timerState.isRunning) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        text = "RUNNING",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // If timer is running or paused, show countdown
                        if (timerState.isRunning || timerState.isPaused) {
                            val mins = timerState.remainingSeconds / 60
                            val secs = timerState.remainingSeconds % 60
                            val timeStr = String.format("%02d:%02d", mins, secs)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = FirePrimaryContainer
                                    )
                                    Text(
                                        text = timerState.bookTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (timerState.isRunning) {
                                        IconButton(
                                            onClick = { ReadingTimerManager.pauseTimer(context) },
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Pause,
                                                contentDescription = "Pause",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            onClick = { ReadingTimerManager.resumeTimer(context) },
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(FirePrimaryContainer)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Resume",
                                                tint = Color.White
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { ReadingTimerManager.cancelTimer(context) },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "Stop",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        } else {
                            // Preset sprint chips (15 min, 20 min, 25 min, 30 min)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(15, 20, 25, 30).forEach { mins ->
                                    val isSelected = selectedSprintMinutes == mins
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) FirePrimaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedSprintMinutes = mins }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${mins}m",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                                ),
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    ReadingTimerManager.startTimer(context, selectedSprintMinutes, "Focus Reading Sprint")
                                    Toast.makeText(context, "Sprint started! Stay focused for $selectedSprintMinutes min.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FirePrimary),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Start Sprint (${selectedSprintMinutes} min)",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 8B. INDIVIDUAL READING SESSIONS LOG (Room DB)
                // ==========================================
                ReadingSessionsSection(
                    sessions = sessions,
                    books = books,
                    defaultMinutes = dailyGoalMins,
                    onLogSession = { bookTitle, minutesRead, pagesRead, notes ->
                        showConfetti = true
                        onLogSession(bookTitle, minutesRead, pagesRead, notes)
                    },
                    onDeleteSession = onDeleteSession
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 8C. YEARLY ACTIVITY HEATMAP
                // ==========================================
                YearlyActivityHeatmap(
                    logs = logs,
                    sessions = sessions,
                    dailyGoalMins = dailyGoalMins
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 9. SPONSORED NATIVE AD CARD
                // ==========================================
                NativeAdBannerCard()

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 10. CONSISTENCY GRID / INSIGHTS ENTRY CARD
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenInsights?.invoke() ?: onOpenWidgetPreviews() }
                        .testTag("consistency_grid_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 7x3 Matrix dots representation
                            Column(
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(end = 14.dp)
                            ) {
                                repeat(3) { r ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        repeat(6) { c ->
                                            val isLit = (r * 6 + c) % 3 != 0
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isLit) FirePrimaryContainer.copy(alpha = 0.85f)
                                                        else MaterialTheme.colorScheme.surfaceContainerHighest
                                                    )
                                            )
                                        }
                                    }
                                }
                            }

                            Column {
                                Text(
                                    text = "Consistency Grid",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "View habit trends and complete analytics",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "View insights",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(110.dp))
            }
        }

        // Celebration Confetti overlay when quest or reading is completed!
        if (showConfetti) {
            CelebrationConfetti(modifier = Modifier.fillMaxSize())
        }
    }
}
