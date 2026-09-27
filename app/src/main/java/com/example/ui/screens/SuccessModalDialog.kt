package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.CelebrationConfetti
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.FirePrimaryContainer
import com.example.ui.theme.FirePrimaryFixed
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import java.text.NumberFormat
import java.util.Locale

private val TertiaryTeal = Color(0xFF00677E)
private val AmberGold = Color(0xFFF59E0B)

@Composable
fun SuccessModalDialog(
    currentStreak: Int,
    loggedMinutes: Int = 25,
    loggedPages: Int = 18,
    baseXp: Int = 50,
    xpGained: Int = 75,
    gemsGained: Int = 5,
    boostMultiplier: String = "1.5x",
    userLevel: Int = 7,
    userTitle: String = "Flamekeeper",
    currentXp: Int = 1525,
    xpForNextLevel: Int = 2000,
    onSaveSessionNote: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val displayStreak = currentStreak.coerceAtLeast(1)
    val previousStreak = (displayStreak - 1).coerceAtLeast(0)

    // Milestone calculation (e.g., Flame Crown II at 15 Days)
    val milestones = listOf(3, 7, 15, 21, 30, 50, 75, 100, 150, 200, 365)
    val nextMilestone = milestones.firstOrNull { it > displayStreak } ?: (displayStreak + 15)
    val daysToMilestone = (nextMilestone - displayStreak).coerceAtLeast(1)

    // Pop-up scale animation for modal card
    val scaleAnim = remember { Animatable(0.84f) }
    var progressTriggered by remember { mutableStateOf(false) }
    var showNotesEditor by remember { mutableStateOf(false) }
    var sessionNoteText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}

        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        progressTriggered = true
    }

    // Infinite pulse and bounce animations for the flame badge
    val infiniteTransition = rememberInfiniteTransition(label = "modal_flame_anim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    val flameBounceOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_bounce"
    )

    val targetXpFraction = (currentXp.toFloat() / xpForNextLevel.toFloat()).coerceIn(0f, 1f)
    val animatedXpProgress by animateFloatAsState(
        targetValue = if (progressTriggered) targetXpFraction else 0.1f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "xp_bar_progress"
    )

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val formattedCurrentXp = remember(currentXp) { numberFormat.format(currentXp) }
    val formattedNextXp = remember(xpForNextLevel) { numberFormat.format(xpForNextLevel) }
    val xpRemaining = (xpForNextLevel - currentXp).coerceAtLeast(0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Full-screen Dark Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.76f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Celebratory Falling & Bursting Confetti Portal
            CelebrationConfetti(
                particleCount = 90,
                burstFromCenter = true
            )

            // Main Gamified Card
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .scale(scaleAnim.value)
                    .shadow(24.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, OutlineVariant, RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume card clicks
                    )
                    .testTag("session_complete_modal_card")
            ) {
                // Background Glow and Ambient Orbs
                Box(
                    modifier = Modifier
                        .size(176.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 56.dp, y = (-56).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    FirePrimaryContainer.copy(alpha = 0.22f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(144.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = (-56).dp, y = (-20).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    SuccessGreenContainer.copy(alpha = 0.24f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Top Bar with Session Tag ("Daily Quest Cleared") and Close Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SuccessGreenContainer.copy(alpha = 0.30f),
                            border = BorderStroke(1.dp, SuccessGreenContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "DAILY QUEST CLEARED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp
                                    ),
                                    color = SuccessGreen
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .testTag("modal_top_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close modal",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Main Inner Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 2. Glowing Flame Streak Badge + Streak Step Pill (11 -> 12 STREAK)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            // Ambient glowing aura behind badge
                            Box(
                                modifier = Modifier
                                    .size(116.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                FirePrimaryContainer.copy(alpha = glowAlpha),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            // Gradient Ring Circle (w-24 h-24 = 96.dp)
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .shadow(12.dp, CircleShape, spotColor = FirePrimaryContainer)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(FirePrimary, FirePrimaryContainer)
                                        )
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Inner Surface Circle with PrimaryFixed border
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(2.dp, FirePrimaryFixed, CircleShape),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak Flame",
                                        tint = FirePrimaryContainer,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .offset(y = flameBounceOffset.dp)
                                    )
                                    Text(
                                        text = "$displayStreak DAYS",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-0.4).sp
                                        ),
                                        color = FirePrimary
                                    )
                                }
                            }

                            // Streak Step Pill (11 -> 12 STREAK) overlapping bottom
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                shadowElevation = 4.dp,
                                border = BorderStroke(1.dp, FirePrimary.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "$previousStreak",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = TextDecoration.LineThrough
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = FirePrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "$displayStreak STREAK",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = FirePrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Main Title & Prompt
                        Text(
                            text = "Session Complete! 🔥",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "You kept the flame alive! $loggedMinutes mins logged • $loggedPages pages read",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. Rewards Grid Cards (2 Columns: XP Gained & Loot Drop)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Left Card: XP Gained
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("modal_xp_gained_card"),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "XP GAINED",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = FirePrimaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = "+$xpGained",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black
                                            ),
                                            color = FirePrimary
                                        )
                                        Text(
                                            text = "XP",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = FirePrimaryContainer,
                                            modifier = Modifier.padding(bottom = 3.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = buildAnnotatedString {
                                            append("+$baseXp base ")
                                            withStyle(
                                                SpanStyle(
                                                    color = SuccessGreen,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            ) {
                                                append("+ $boostMultiplier")
                                            }
                                            append(" streak multiplier")
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            lineHeight = 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Right Card: Loot Drop (Gems & Perks)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("modal_loot_drop_card"),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "LOOT DROP",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Diamond,
                                            contentDescription = null,
                                            tint = TertiaryTeal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "+$gemsGained",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black
                                            ),
                                            color = TertiaryTeal
                                        )
                                        Text(
                                            text = "Gems",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = TertiaryTeal,
                                            modifier = Modifier.padding(bottom = 3.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "Streak Shield safe",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                lineHeight = 12.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 5. Level Progress Bar Card (Goal Gradient)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(FirePrimaryContainer.copy(alpha = 0.20f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$userLevel",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = FirePrimary
                                            )
                                        }
                                        Text(
                                            text = "Level $userLevel $userTitle",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Text(
                                        text = "$formattedCurrentXp / $formattedNextXp XP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = FirePrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Progress Track
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(animatedXpProgress)
                                            .height(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(FirePrimary, FirePrimaryContainer)
                                                )
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Only $xpRemaining XP to Level ${userLevel + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = FirePrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Phoenix Quill",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = FirePrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 6. Milestone Tracker Card (Flame Crown II)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MilitaryTech,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Flame Crown II ($nextMilestone Days)",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$daysToMilestone consecutive ${if (daysToMilestone == 1) "day" else "days"} remaining",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // 3 Progress Indicator Dots
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(FirePrimaryContainer)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(FirePrimaryContainer)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, OutlineVariant, CircleShape)
                                    )
                                }
                            }
                        }

                        // Optional inline Session Notes Editor when toggled
                        AnimatedVisibility(visible = showNotesEditor) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                OutlinedTextField(
                                    value = sessionNoteText,
                                    onValueChange = { sessionNoteText = it },
                                    placeholder = {
                                        Text(
                                            text = "Add a quick reflection or favorite quote from today's reading...",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("session_notes_input"),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 7. CTAs: Claim Rewards & Secondary Options
                        Button(
                            onClick = {
                                if (sessionNoteText.isNotBlank()) {
                                    onSaveSessionNote(sessionNoteText.trim())
                                }
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = FirePrimary)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(FirePrimary, FirePrimaryContainer)
                                    )
                                )
                                .testTag("close_success_modal_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Claim Rewards & Continue (+$xpGained XP)",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Secondary Buttons Row: Share Streak & Session Notes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "I just completed a $displayStreak-day reading streak on Read Streak! 🔥 ($loggedMinutes mins • $loggedPages pages read today)"
                                        )
                                    }
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "Share Reading Streak")
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("share_streak_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, OutlineVariant),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = FirePrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Share Streak",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    if (showNotesEditor && sessionNoteText.isNotBlank()) {
                                        onSaveSessionNote(sessionNoteText.trim())
                                        Toast.makeText(context, "Session note saved!", Toast.LENGTH_SHORT).show()
                                        showNotesEditor = false
                                    } else {
                                        showNotesEditor = !showNotesEditor
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("session_notes_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, OutlineVariant),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showNotesEditor && sessionNoteText.isNotBlank()) "Save Note" else "Session Notes",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
