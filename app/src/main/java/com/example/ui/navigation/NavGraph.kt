package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import com.example.timer.ReadingTimerManager
import com.example.ui.components.ReadingTimerClaimDialog
import com.example.ui.components.FloatingNavDock
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BooksScreen
import com.example.ui.screens.GoProBottomSheet
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StreakHomeScreen
import com.example.ui.screens.SuccessModalDialog
import com.example.ui.screens.WidgetPreviewsScreen
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.OutlineVariant
import com.example.viewmodel.ReadStreakViewModel

sealed class BottomTab(val route: String, val title: String, val icon: ImageVector) {
    object Streak : BottomTab("tab_streak", "Streak", Icons.Default.LocalFireDepartment)
    object Books : BottomTab("tab_books", "Books", Icons.Default.AutoStories)
    object Stats : BottomTab("tab_stats", "Stats", Icons.Default.BarChart)
    object Profile : BottomTab("tab_profile", "Profile", Icons.Default.Person)
}

object NavRoute {
    const val Onboarding = "onboarding"
    const val Main = "main"
    const val Settings = "settings"
    const val WidgetPreviews = "widget_previews"
    const val Auth = "auth"
}

@Composable
fun ReadStreakApp(viewModel: ReadStreakViewModel) {
    val navController = rememberNavController()

    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val bestStreak by viewModel.bestStreak.collectAsStateWithLifecycle()
    val totalDaysRead by viewModel.totalDaysRead.collectAsStateWithLifecycle()
    val completedDaysGoal by viewModel.completedDaysGoal.collectAsStateWithLifecycle()
    val weekDaysRead by viewModel.weekDaysRead.collectAsStateWithLifecycle()
    val isTodayRead by viewModel.isTodayRead.collectAsStateWithLifecycle()
    val showSuccessModal by viewModel.showSuccessModal.collectAsStateWithLifecycle()
    val showGoProPaywall by viewModel.showGoProPaywall.collectAsStateWithLifecycle()
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val hapticFeedback by viewModel.hapticFeedback.collectAsStateWithLifecycle()
    val dailyPrompt by viewModel.dailyPrompt.collectAsStateWithLifecycle()
    val reminderTime by viewModel.reminderTime.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val dailyGoalMinutes by viewModel.dailyGoalMinutes.collectAsStateWithLifecycle()
    val dailyGoalPages by viewModel.dailyGoalPages.collectAsStateWithLifecycle()
    val dailyGoalType by viewModel.dailyGoalType.collectAsStateWithLifecycle()
    val userLevel by viewModel.userLevel.collectAsStateWithLifecycle()
    val currentXp by viewModel.currentXp.collectAsStateWithLifecycle()
    val gemsCount by viewModel.gemsCount.collectAsStateWithLifecycle()
    val isBoostActive by viewModel.isBoostActive.collectAsStateWithLifecycle()
    val todayMinutesRead by viewModel.todayMinutesRead.collectAsStateWithLifecycle()
    val isDailyQuestClaimed by viewModel.isDailyQuestClaimed.collectAsStateWithLifecycle()
    val lastLoggedMinutes by viewModel.lastLoggedMinutes.collectAsStateWithLifecycle()
    val lastLoggedPages by viewModel.lastLoggedPages.collectAsStateWithLifecycle()
    val lastBaseXp by viewModel.lastBaseXp.collectAsStateWithLifecycle()
    val lastXpGained by viewModel.lastXpGained.collectAsStateWithLifecycle()
    val lastGemsGained by viewModel.lastGemsGained.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val timerState by ReadingTimerManager.timerState.collectAsStateWithLifecycle()

    var activeSponsoredOfferId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        ReadingTimerManager.updateStateFromPrefs(context)

        val activity = context as? android.app.Activity
        val offerId = activity?.intent?.getStringExtra("OPEN_SPONSORED_OFFER_ID")
        if (!offerId.isNullOrEmpty()) {
            activeSponsoredOfferId = offerId
            activity.intent?.removeExtra("OPEN_SPONSORED_OFFER_ID")
        }
    }

    if (!activeSponsoredOfferId.isNullOrEmpty()) {
        val offer = com.example.notification.NotificationAdManager.getOfferById(activeSponsoredOfferId!!)
        com.example.ui.components.SponsoredOfferDialog(
            offer = offer,
            onDismiss = { activeSponsoredOfferId = null }
        )
    }

    if (timerState.isFinished) {
        ReadingTimerClaimDialog(
            initialMinutes = timerState.totalMinutes,
            initialBookTitle = timerState.bookTitle,
            onDismiss = {
                ReadingTimerManager.clearFinishedState(context)
            },
            onClaimSession = { mins, book, pages ->
                ReadingTimerManager.clearFinishedState(context)
                viewModel.logCompletedTimerSession(mins, book, pages)
            }
        )
    }

    val startDestination = when {
        currentUser == null -> NavRoute.Auth
        !hasCompletedOnboarding -> NavRoute.Onboarding
        else -> NavRoute.Main
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoute.Onboarding) {
            OnboardingScreen(
                currentUser = currentUser,
                initialGoalMinutes = dailyGoalMinutes,
                onFinishOnboarding = { selectedGoalMinutes ->
                    viewModel.setDailyGoalMinutes(selectedGoalMinutes)
                    viewModel.completeOnboarding()
                    navController.navigate(NavRoute.Main) {
                        popUpTo(NavRoute.Onboarding) { inclusive = true }
                    }
                },
                onOpenAuth = {
                    viewModel.clearAuthError()
                    navController.navigate(NavRoute.Auth)
                }
            )
        }

        composable(NavRoute.Main) {
            var selectedTab by remember { mutableStateOf<BottomTab>(BottomTab.Streak) }
            var showReadingAdDialog by remember { mutableStateOf(false) }

            if (showReadingAdDialog) {
                com.example.ui.components.ads.InterstitialAdDialog(
                    adTitle = "Sponsor Interstitial",
                    onAdClosed = {
                        showReadingAdDialog = false
                        viewModel.logTodayReading()
                    }
                )
            }

            Scaffold(
                bottomBar = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        com.example.ui.components.ads.StickyBottomBannerAd()
                        FloatingNavDock(
                            selectedTab = selectedTab,
                            isSettingsSelected = false,
                            isTodayRead = isTodayRead,
                            onTabSelected = { selectedTab = it },
                            onLogTodayRead = { showReadingAdDialog = true },
                            onOpenSettings = { navController.navigate(NavRoute.Settings) }
                        )
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        BottomTab.Streak -> {
                            StreakHomeScreen(
                                currentUser = currentUser,
                                currentStreak = currentStreak,
                                bestStreak = bestStreak,
                                completedDays = completedDaysGoal,
                                yearlyGoal = viewModel.yearlyGoal,
                                weekDays = weekDaysRead,
                                isTodayRead = isTodayRead,
                                timerState = timerState,
                                dailyGoalMins = dailyGoalMinutes,
                                userLevel = userLevel,
                                userTitle = viewModel.userTitleForLevel(userLevel),
                                currentXp = currentXp,
                                xpForNextLevel = viewModel.xpForNextLevel,
                                gemsCount = gemsCount,
                                isBoostActive = isBoostActive,
                                boostMultiplier = viewModel.boostMultiplier,
                                todayMinutesRead = todayMinutesRead,
                                isDailyQuestClaimed = isDailyQuestClaimed,
                                logs = allLogs,
                                sessions = allSessions,
                                books = allBooks,
                                onClaimDailyQuest = { viewModel.claimDailyQuest() },
                                onLogCompletedMinutes = { mins, book -> viewModel.logCompletedTimerSession(mins, book) },
                                onLogSession = { bookTitle, mins, pages, notes ->
                                    viewModel.logReadingSession(bookTitle, mins, pages, notes)
                                },
                                onDeleteSession = { sessionId ->
                                    viewModel.deleteReadingSession(sessionId)
                                },
                                onLogTodayRead = { showReadingAdDialog = true },
                                onOpenSettings = { navController.navigate(NavRoute.Settings) },
                                onOpenWidgetPreviews = { navController.navigate(NavRoute.WidgetPreviews) },
                                onOpenAuth = {
                                    viewModel.clearAuthError()
                                    navController.navigate(NavRoute.Auth)
                                },
                                onOpenInsights = { selectedTab = BottomTab.Stats }
                            )
                        }
                        BottomTab.Books -> {
                            BooksScreen(
                                books = allBooks,
                                onAddBook = { title, author, pages -> viewModel.addBook(title, author, pages) },
                                onUpdatePages = { book, newPages -> viewModel.updateBookPages(book, newPages) },
                                onDeleteBook = { bookId -> viewModel.deleteBook(bookId) }
                            )
                        }
                        BottomTab.Stats -> {
                            val avgSessionMins = if (allLogs.isEmpty()) 0 else (allLogs.sumOf { it.minutesRead } / allLogs.size)
                            InsightsScreen(
                                totalDaysRead = totalDaysRead,
                                longestStreak = bestStreak,
                                avgSessionMins = avgSessionMins,
                                logs = allLogs,
                                sessions = allSessions,
                                books = allBooks,
                                currentStreak = currentStreak,
                                dailyGoalMins = dailyGoalMinutes,
                                onOpenGoProPaywall = { viewModel.openGoProPaywall() },
                                onOpenSettings = { navController.navigate(NavRoute.Settings) }
                            )
                        }
                        BottomTab.Profile -> {
                            ProfileScreen(
                                currentUser = currentUser,
                                currentStreak = currentStreak,
                                bestStreak = bestStreak,
                                totalDaysRead = totalDaysRead,
                                completedBooksCount = allBooks.count { it.isCompleted },
                                isTodayRead = isTodayRead,
                                onLogTodayRead = { viewModel.logTodayReading() },
                                onOpenSettings = { navController.navigate(NavRoute.Settings) },
                                onOpenAuth = {
                                    viewModel.clearAuthError()
                                    navController.navigate(NavRoute.Auth)
                                },
                                onLogout = {
                                    viewModel.logoutUser {
                                        navController.navigate(NavRoute.Auth)
                                    }
                                },
                                onUpdateDisplayName = { newName ->
                                    viewModel.updateUserDisplayName(newName)
                                },
                                onUpdateProfilePhoto = { photoUri ->
                                    viewModel.updateUserProfilePhoto(photoUri)
                                },
                                onOpenGoProPaywall = { viewModel.openGoProPaywall() }
                            )
                        }
                    }
                }
            }
        }

        composable(NavRoute.Settings) {
            val context = androidx.compose.ui.platform.LocalContext.current
            Scaffold(
                bottomBar = {
                    FloatingNavDock(
                        selectedTab = null,
                        isSettingsSelected = true,
                        isTodayRead = isTodayRead,
                        onTabSelected = { tab ->
                            navController.popBackStack()
                        },
                        onLogTodayRead = { viewModel.logTodayReading() },
                        onOpenSettings = { /* Already on settings */ }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    SettingsScreen(
                        currentUser = currentUser,
                        themeMode = themeMode,
                        hapticFeedback = hapticFeedback,
                        dailyPrompt = dailyPrompt,
                        reminderTime = reminderTime,
                        dailyGoalMinutes = dailyGoalMinutes,
                        dailyGoalPages = dailyGoalPages,
                        dailyGoalType = dailyGoalType,
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSetHapticFeedback = { viewModel.setHapticFeedback(it) },
                        onSetDailyPrompt = { viewModel.setDailyPrompt(context, it) },
                        onSetReminderTime = { viewModel.setReminderTime(context, it) },
                        onSetDailyGoalMinutes = { viewModel.setDailyGoalMinutes(it) },
                        onSetDailyGoalPages = { viewModel.setDailyGoalPages(it) },
                        onSetDailyGoalType = { viewModel.setDailyGoalType(it) },
                        onOpenGoProPaywall = { viewModel.openGoProPaywall() },
                        onOpenWidgetPreviews = { navController.navigate(NavRoute.WidgetPreviews) },
                        onOpenAuth = {
                            viewModel.clearAuthError()
                            navController.navigate(NavRoute.Auth)
                        },
                        onLogout = {
                            viewModel.logoutUser {
                                navController.navigate(NavRoute.Auth)
                            }
                        },
                        onDeleteAllProgress = { viewModel.resetAllProgress() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        composable(NavRoute.WidgetPreviews) {
            WidgetPreviewsScreen(
                currentStreak = currentStreak,
                onLogTodayRead = { viewModel.logTodayReading() },
                onOpenSettings = { navController.navigate(NavRoute.Settings) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoute.Auth) {
            val navigatePostAuth = {
                if (!hasCompletedOnboarding) {
                    navController.navigate(NavRoute.Onboarding) {
                        popUpTo(NavRoute.Auth) { inclusive = true }
                    }
                } else if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                } else {
                    navController.navigate(NavRoute.Main) {
                        popUpTo(NavRoute.Auth) { inclusive = true }
                    }
                }
            }

            AuthScreen(
                errorMessage = authError,
                onLogin = { emailOrUsername, password ->
                    viewModel.loginUser(emailOrUsername, password) {
                        navigatePostAuth()
                    }
                },
                onRegister = { username, email, password, displayName ->
                    viewModel.registerUser(username, email, password, displayName) {
                        navigatePostAuth()
                    }
                },
                onBack = if (navController.previousBackStackEntry != null) {
                    {
                        viewModel.clearAuthError()
                        navController.popBackStack()
                    }
                } else null
            )
        }
    }

    // Global Success Modal Dialog
    if (showSuccessModal) {
        SuccessModalDialog(
            currentStreak = currentStreak,
            loggedMinutes = lastLoggedMinutes,
            loggedPages = lastLoggedPages,
            baseXp = lastBaseXp,
            xpGained = lastXpGained,
            gemsGained = lastGemsGained,
            boostMultiplier = viewModel.boostMultiplier,
            userLevel = userLevel,
            userTitle = viewModel.userTitleForLevel(userLevel),
            currentXp = currentXp,
            xpForNextLevel = viewModel.xpForNextLevel,
            onSaveSessionNote = { note -> viewModel.saveSessionNote(note) },
            onDismiss = { viewModel.dismissSuccessModal() }
        )
    }
}
