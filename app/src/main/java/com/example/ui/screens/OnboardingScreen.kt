package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserEntity
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.FirePrimaryContainer
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val buttonText: String,
    val isGoalPage: Boolean = false,
    val isLastPage: Boolean = false
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    currentUser: UserEntity? = null,
    initialGoalMinutes: Int = 20,
    onFinishOnboarding: (selectedGoalMinutes: Int) -> Unit,
    onOpenAuth: () -> Unit = {}
) {
    var selectedGoalMinutes by remember { mutableIntStateOf(initialGoalMinutes) }

    val hoursPerYear = (selectedGoalMinutes * 365) / 60
    val approxBooksYearly = (hoursPerYear * 60) / 300

    val pages = remember(selectedGoalMinutes, hoursPerYear, approxBooksYearly) {
        listOf(
            OnboardingPageData(
                title = "Build a reading habit",
                description = "One tap a day. That's all it takes to keep your daily momentum.",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD2kS6YFlRvIRWz_MK1ZOsqsMf89_Oy6fcvWtRIObMOXV6Irl0kb2HmF6MIcvXMO5I7tlIbU2rveDacd5fmQ-fNBZoCLlL7O0VnRYAiAIvsm6_nVUbtjYxs08MgnxSWqCI-8RLyoIAG61qdaI30fnO3jkF1t3LcS9lxcUNL030zHSbsMRfdFDjWIG2y8YuRF-CYG5Hbvs6gqwTHVWP7YW579DIk3KtQDZlkUHq-redmaUB0caiSottP3g",
                buttonText = "Next",
                isGoalPage = false,
                isLastPage = false
            ),
            OnboardingPageData(
                title = "Keep your streak alive",
                description = "Visualize your progress, earn streaks, and never break the chain.",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBJtcslYD3NkEIKN9WifhAFOOQcA0ZKh8sR_PceuGPWTMVumOyi8HUZI83GYDy1QvHEKXOh81A0-TWkFGNkdJlx9c5Oek7XtRJN9DI2Y3Ar8VZZsDpFZaOjo9skiXA-I3lZ7ZuJP6oHYH4FIrOfshkOeU3SlGYCu2t3nHt7lozDZUalN-0_ddrxFnJAZ4BmecgmtJRTyBIeBT-RyCdhyZvVOpNMKfeLYZ1n0bYhLOvMHPKEk4RKEwGcJw",
                buttonText = "Next",
                isGoalPage = false,
                isLastPage = false
            ),
            OnboardingPageData(
                title = "Set your daily goal",
                description = "Choose your daily reading target. At $selectedGoalMinutes mins/day, you'll read ~$hoursPerYear hours a year!",
                imageUrl = null,
                buttonText = "Set Goal & Continue",
                isGoalPage = true,
                isLastPage = false
            ),
            OnboardingPageData(
                title = "Ready to launch!",
                description = "Your $selectedGoalMinutes mins/day goal (~$hoursPerYear hrs / $approxBooksYearly books a year) is configured. Let's start reading!",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBDoK4EPP7bwNLAi_AOLg9FP6rTgm2yT08OvuWNTKRfAzHhUS0flJh-IhM1n5MCmJ8an749UDQqzTrSZPbMiy4pGokduRrgszVgQiA3DIxIbxvdHZyBSXZmyHHwCoorUvfpYtJYlwfK0wYRNyNlORq3bpUf8hHXMBTlvsJ9xwKb9LB4jgTDAHpGzopDHlnmhFI5xyhQCLkiNqbd7PZtGeH_WSODFm0om5nJHCdFgK3AA5G3goZlY8ztBw",
                buttonText = "Start Reading",
                isGoalPage = false,
                isLastPage = true
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Lightweight Atmospheric Background Glows (Zero GPU Blur Overhead)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(280.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(FirePrimary.copy(alpha = 0.16f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(240.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF6FFB85).copy(alpha = 0.14f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentUser == null) {
                    TextButton(
                        onClick = onOpenAuth,
                        modifier = Modifier.testTag("onboarding_auth_button")
                    ) {
                        Text(
                            text = "Log In / Sign Up",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = FirePrimary
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FirePrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.displayName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = FirePrimary,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Welcome, ${currentUser.displayName}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = { onFinishOnboarding(selectedGoalMinutes) },
                        modifier = Modifier.testTag("skip_onboarding_button")
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (page.isGoalPage) {
                        // GOAL SELECTOR COMPONENT
                        GoalSelectionContent(
                            selectedGoalMinutes = selectedGoalMinutes,
                            onSelectGoal = { selectedGoalMinutes = it }
                        )
                    } else {
                        // STANDARD ONBOARDING HERO ILLUSTRATION & TEXT (Instant Zero-Network Vector Art)
                        page.imageUrl?.let {
                            val heroIcon: ImageVector = when (pageIndex) {
                                0 -> Icons.Default.AutoStories
                                1 -> Icons.Default.LocalFireDepartment
                                else -> Icons.Default.RocketLaunch
                            }
                            val accentEnd = when (pageIndex) {
                                0 -> Color(0xFFFF8A50)
                                1 -> Color(0xFFFFB300)
                                else -> Color(0xFF00C853)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.82f)
                                    .aspectRatio(1.1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                FirePrimary.copy(alpha = 0.14f),
                                                accentEnd.copy(alpha = 0.22f)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = FirePrimary.copy(alpha = 0.22f),
                                        shape = RoundedCornerShape(24.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    FirePrimary.copy(alpha = 0.25f),
                                                    Color.Transparent
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(FirePrimary, FirePrimaryContainer)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = heroIcon,
                                            contentDescription = page.title,
                                            tint = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = page.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = page.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // Page Indicators & Bottom Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { i ->
                        val isSelected = pagerState.currentPage == i
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) FirePrimary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val page = pages[pagerState.currentPage]
                Button(
                    onClick = {
                        if (page.isLastPage) {
                            onFinishOnboarding(selectedGoalMinutes)
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("onboarding_next_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FirePrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = page.buttonText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (page.isLastPage) Icons.Default.RocketLaunch else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalSelectionContent(
    selectedGoalMinutes: Int,
    onSelectGoal: (Int) -> Unit
) {
    val goalOptions = listOf(
        10 to "Light Reader",
        15 to "Steady Reader",
        20 to "Recommended",
        30 to "Dedicated",
        45 to "Avid Reader",
        60 to "Bookworm"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(FirePrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = FirePrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Set Your Daily Reading Goal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Consistency beats intensity. Choose a daily pace to see your projected yearly progress.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Goal Selection Grid / List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            goalOptions.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { (mins, label) ->
                        val isSelected = selectedGoalMinutes == mins
                        val itemHours = (mins * 365) / 60
                        val backgroundColor by animateColorAsState(
                            targetValue = if (isSelected) FirePrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            animationSpec = tween(durationMillis = 200),
                            label = "bg"
                        )
                        val borderColor by animateColorAsState(
                            targetValue = if (isSelected) FirePrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            animationSpec = tween(durationMillis = 200),
                            label = "border"
                        )

                        Surface(
                            onClick = { onSelectGoal(mins) },
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .testTag("goal_option_${mins}_mins"),
                            shape = RoundedCornerShape(16.dp),
                            color = backgroundColor,
                            border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "$mins Mins/day",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isSelected) FirePrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$label (~$itemHours hrs/yr)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) FirePrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = FirePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Live Summary Banner recalculated based on selectedGoalMinutes
        val hoursPerYear = (selectedGoalMinutes * 365) / 60
        val booksPerYear = (hoursPerYear * 60) / 300

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(FirePrimaryContainer.copy(alpha = 0.7f))
                .border(1.dp, FirePrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = FirePrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Goal: $selectedGoalMinutes mins/day = ~$hoursPerYear hrs/year (~$booksPerYear books/year)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = FirePrimary
                )
            }
        }
    }
}
