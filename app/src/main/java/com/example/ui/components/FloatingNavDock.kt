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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.BottomTab
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.FirePrimaryContainer
import com.example.ui.theme.OnSuccessGreenContainer
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.SuccessGreenContainer

@Composable
fun FloatingNavDock(
    selectedTab: BottomTab?,
    isSettingsSelected: Boolean = false,
    isTodayRead: Boolean,
    onTabSelected: (BottomTab) -> Unit,
    onLogTodayRead: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for Center FAB when today is not logged
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val fabPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_pulse_scale"
    )

    // Scale interaction state for Center FAB button
    val fabInteractionSource = remember { MutableInteractionSource() }
    val isFabPressed by fabInteractionSource.collectIsPressedAsState()
    val animatedFabScale by animateFloatAsState(
        targetValue = if (isFabPressed) 0.92f else if (!isTodayRead) fabPulseScale else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "animated_fab_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.25f))
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, OutlineVariant.copy(alpha = 0.6f), RoundedCornerShape(32.dp)),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home / Streak Tab
                DockNavButton(
                    title = "Streak",
                    icon = Icons.Default.LocalFireDepartment,
                    isSelected = selectedTab == BottomTab.Streak && !isSettingsSelected,
                    testTag = "nav_tab_streak",
                    onClick = { onTabSelected(BottomTab.Streak) },
                    modifier = Modifier.weight(1f)
                )

                // 2. Books Tab
                DockNavButton(
                    title = "Books",
                    icon = Icons.Default.AutoStories,
                    isSelected = selectedTab == BottomTab.Books && !isSettingsSelected,
                    testTag = "nav_tab_books",
                    onClick = { onTabSelected(BottomTab.Books) },
                    modifier = Modifier.weight(1f)
                )

                // 3. Prominent Center FAB ("Read Today")
                Box(
                    modifier = Modifier
                        .scale(animatedFabScale)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isTodayRead) {
                                Brush.horizontalGradient(
                                    colors = listOf(SuccessGreenContainer, SuccessGreenContainer)
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF6B35), FirePrimaryContainer)
                                )
                            }
                        )
                        .clickable(
                            interactionSource = fabInteractionSource,
                            indication = null,
                            onClick = onLogTodayRead
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("floating_dock_center_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isTodayRead) Icons.Default.Check else Icons.Default.LocalFireDepartment,
                            contentDescription = if (isTodayRead) "Logged" else "Log Reading",
                            tint = if (isTodayRead) OnSuccessGreenContainer else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTodayRead) "Done" else "Read",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isTodayRead) OnSuccessGreenContainer else Color.White
                        )
                    }
                }

                // 4. Stats / Insights Tab
                DockNavButton(
                    title = "Stats",
                    icon = Icons.Default.BarChart,
                    isSelected = selectedTab == BottomTab.Stats && !isSettingsSelected,
                    testTag = "nav_tab_stats",
                    onClick = { onTabSelected(BottomTab.Stats) },
                    modifier = Modifier.weight(1f)
                )

                // 5. Profile Tab
                DockNavButton(
                    title = "Profile",
                    icon = Icons.Default.Person,
                    isSelected = selectedTab == BottomTab.Profile && !isSettingsSelected,
                    testTag = "nav_tab_profile",
                    onClick = { onTabSelected(BottomTab.Profile) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DockNavButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "nav_button_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .scale(buttonScale)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) FirePrimary.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) FirePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) FirePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1
        )
    }
}
