package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoCamera
import com.example.ui.components.LocalAvatarImage
import com.example.R
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.FirePrimaryContainer
import com.example.ui.theme.OnSuccessGreenContainer
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.SuccessGreenContainer

import android.widget.Toast
import java.io.File
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.OutlinedButton
import com.example.data.UserEntity

data class AchievementItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconTint: Color,
    val isUnlocked: Boolean
)

@Composable
fun ProfileScreen(
    currentUser: UserEntity? = null,
    currentStreak: Int,
    bestStreak: Int,
    totalDaysRead: Int,
    completedBooksCount: Int,
    isTodayRead: Boolean,
    onLogTodayRead: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAuth: () -> Unit = {},
    onLogout: () -> Unit = {},
    onUpdateDisplayName: (String) -> Unit = {},
    onUpdateProfilePhoto: (String?) -> Unit = {},
    onOpenGoProPaywall: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    var isEditingName by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var showUrlInputDialog by remember { mutableStateOf(false) }
    var selectedAchievement by remember { mutableStateOf<AchievementItem?>(null) }
    val displayName = currentUser?.displayName ?: "Reader"

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onUpdateProfilePhoto(it.toString())
        }
    }

    val apkDownloadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { uri ->
        uri?.let { targetUri ->
            try {
                val apkFile = File(context.applicationInfo.sourceDir)
                context.contentResolver.openOutputStream(targetUri)?.use { output ->
                    apkFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                Toast.makeText(context, "ReadStreak.apk saved successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save APK: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val achievements = remember(totalDaysRead, bestStreak, colorScheme) {
        listOf(
            AchievementItem(
                title = "First Chapter",
                description = "Logged your first reading day to start your streak!",
                icon = Icons.Default.MenuBook,
                backgroundColor = colorScheme.secondaryContainer,
                iconTint = colorScheme.onSecondaryContainer,
                isUnlocked = totalDaysRead >= 1
            ),
            AchievementItem(
                title = "Habit Builder",
                description = "Logged 5 days of reading toward a permanent habit.",
                icon = Icons.Default.WbSunny,
                backgroundColor = colorScheme.tertiaryContainer,
                iconTint = colorScheme.onTertiaryContainer,
                isUnlocked = totalDaysRead >= 5
            ),
            AchievementItem(
                title = "30 Days Fire",
                description = "Maintained a continuous 30-day reading streak!",
                icon = Icons.Default.LocalFireDepartment,
                backgroundColor = FirePrimary,
                iconTint = Color.White,
                isUnlocked = bestStreak >= 30
            ),
            AchievementItem(
                title = "Centurion",
                description = "Reach 100 total reading days to unlock this legendary badge.",
                icon = Icons.Default.EmojiEvents,
                backgroundColor = colorScheme.primaryContainer,
                iconTint = colorScheme.onPrimaryContainer,
                isUnlocked = totalDaysRead >= 100
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = FirePrimary
                )

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("profile_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // 2. Profile Header: Identity & Social Proof
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .border(4.dp, FirePrimary, CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .clickable { showPhotoOptionsDialog = true }
                            .padding(4.dp)
                            .testTag("profile_avatar_box")
                    ) {
                        val avatarModel = if (!currentUser?.profilePhotoUri.isNullOrEmpty()) {
                            currentUser.profilePhotoUri
                        } else {
                            "https://api.dicebear.com/7.x/adventurer/png?seed=${currentUser?.username ?: "Alex"}"
                        }
                        LocalAvatarImage(
                            model = avatarModel,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    // Camera Icon Overlay Badge to Edit Photo
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { showPhotoOptionsDialog = true }
                            .shadow(6.dp, CircleShape)
                            .testTag("change_photo_btn"),
                        shape = CircleShape,
                        color = FirePrimary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Change Profile Photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Pro Reader Badge Overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clickable { onOpenGoProPaywall() }
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .testTag("pro_reader_badge"),
                        shape = RoundedCornerShape(20.dp),
                        color = FirePrimary
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFAB3500), Color(0xFFFF6B35))
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Pro Reader",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // Name and Edit Action
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { isEditingName = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Name",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Member Since",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Member since ${currentUser?.createdAt ?: "Jan 2024"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Account Auth Status Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Account Security",
                                    tint = FirePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Account Details",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SuccessGreenContainer
                            ) {
                                Text(
                                    text = "ENCRYPTED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = OnSuccessGreenContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (currentUser != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Email: ${currentUser.email}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Username: @${currentUser.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onOpenAuth,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("switch_account_btn"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = "Switch Account",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Switch Account",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = onLogout,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("logout_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Logout",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Log Out",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onOpenAuth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("sign_in_register_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FirePrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Login,
                                    contentDescription = "Sign In",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sign In or Create Account",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Reading Stats: Loss Aversion & Key Metrics Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 1: Total Days
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$totalDaysRead",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = FirePrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TOTAL DAYS",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Card 2: Best Streak (Primary Highlighted Card with Loss Aversion Shield Indicator)
                    Card(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(110.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = FirePrimaryContainer.copy(alpha = 0.12f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, FirePrimaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$bestStreak",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = FirePrimary
                            )
                            Text(
                                text = "BEST STREAK",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = FirePrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Loss Aversion Shield Indicator
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FirePrimaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Shield Protection",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "2 SHIELDS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Card 3: Books Done
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$completedBooksCount",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = FirePrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "BOOKS DONE",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Gamification: Recent Achievements
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recent_achievements_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECENT ACHIEVEMENTS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${achievements.count { it.isUnlocked }}/${achievements.size} Unlocked",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = FirePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            achievements.forEach { achievement ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { selectedAchievement = achievement }
                                        .padding(4.dp)
                                ) {
                                    val bg = if (achievement.isUnlocked) achievement.backgroundColor else MaterialTheme.colorScheme.surfaceVariant
                                    val iconColor = if (achievement.isUnlocked) achievement.iconTint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    val iconVec = if (achievement.isUnlocked) achievement.icon else Icons.Default.Lock

                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(bg)
                                            .border(
                                                width = if (achievement.isUnlocked) 0.dp else 1.5.dp,
                                                color = OutlineVariant.copy(alpha = 0.6f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconVec,
                                            contentDescription = achievement.title,
                                            tint = iconColor,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = achievement.title,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Personal Bests
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("personal_bests_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "PERSONAL BESTS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Best Item 1
                        PersonalBestRow(
                            title = "Longest Streak",
                            value = "$bestStreak Days",
                            icon = Icons.Default.Bolt
                        )

                        // Best Item 2
                        PersonalBestRow(
                            title = "Most Pages / Day",
                            value = "112 Pages",
                            icon = Icons.Default.MenuBook
                        )

                        // Best Item 3
                        PersonalBestRow(
                            title = "Best Month",
                            value = "Oct '24",
                            icon = Icons.Default.CalendarMonth,
                            isLast = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5B. Direct APK Download Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { apkDownloadLauncher.launch("ReadStreak.apk") }
                        .testTag("download_apk_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FirePrimary.copy(alpha = 0.1f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, FirePrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(FirePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download APK",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Download ReadStreak.apk",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Save the compiled Android APK file directly",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { apkDownloadLauncher.launch("ReadStreak.apk") },
                            colors = ButtonDefaults.buttonColors(containerColor = FirePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("download_apk_btn")
                        ) {
                            Text(
                                text = "Save APK",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 6. Delight & CTA Button ("Log Today's Read")
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val buttonScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.96f else 1.0f,
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                    label = "btn_scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(buttonScale)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = FirePrimary
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isTodayRead) {
                                Brush.horizontalGradient(
                                    colors = listOf(SuccessGreenContainer, SuccessGreenContainer)
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF6B35), Color(0xFFAB3500))
                                )
                            }
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onLogTodayRead
                        )
                        .padding(vertical = 18.dp)
                        .testTag("profile_log_today_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isTodayRead) Icons.Default.Check else Icons.Default.HistoryEdu,
                            contentDescription = if (isTodayRead) "Completed" else "Log Today's Read",
                            tint = if (isTodayRead) OnSuccessGreenContainer else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isTodayRead) "Today's Reading Logged! 🎉" else "Log Today's Read",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isTodayRead) OnSuccessGreenContainer else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }

    // Edit Name Dialog
    if (isEditingName) {
        var tempName by remember { mutableStateOf(displayName) }
        AlertDialog(
            onDismissRequest = { isEditingName = false },
            title = { Text("Edit Display Name") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            onUpdateDisplayName(tempName)
                        }
                        isEditingName = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FirePrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingName = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Achievement Detail Modal Dialog
    selectedAchievement?.let { achievement ->
        AlertDialog(
            onDismissRequest = { selectedAchievement = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(achievement.backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = achievement.icon,
                        contentDescription = achievement.title,
                        tint = achievement.iconTint,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (achievement.isUnlocked) SuccessGreenContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (achievement.isUnlocked) "UNLOCKED 🎉" else "LOCKED 🔒",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (achievement.isUnlocked) OnSuccessGreenContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedAchievement = null },
                    colors = ButtonDefaults.buttonColors(containerColor = FirePrimary)
                ) {
                    Text("Awesome")
                }
            }
        )
    }

    // Change / Upload Profile Photo Dialog
    if (showPhotoOptionsDialog) {
        var showPresets by remember { mutableStateOf(false) }

        val presetAvatars = listOf(
            "https://api.dicebear.com/7.x/adventurer/png?seed=Alex",
            "https://api.dicebear.com/7.x/avataaars/png?seed=Felix",
            "https://api.dicebear.com/7.x/bottts/png?seed=Spike",
            "https://api.dicebear.com/7.x/big-smile/png?seed=Milo",
            "https://api.dicebear.com/7.x/fun-emoji/png?seed=Zoe",
            "https://api.dicebear.com/7.x/lorelei/png?seed=Luna",
            "https://api.dicebear.com/7.x/micah/png?seed=Nova",
            "https://api.dicebear.com/7.x/open-peeps/png?seed=Leo",
            "https://api.dicebear.com/7.x/adventurer/png?seed=Cleo"
        )

        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = {
                Text(
                    text = if (showPresets) "Choose Preset Avatar" else "Change Profile Photo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (showPresets) {
                        Text(
                            text = "Tap any avatar below to set it as your profile picture:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            presetAvatars.chunked(3).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    row.forEach { avatarUrl ->
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, FirePrimary.copy(alpha = 0.6f), CircleShape)
                                                .clickable {
                                                    onUpdateProfilePhoto(avatarUrl)
                                                    showPhotoOptionsDialog = false
                                                }
                                        ) {
                                            LocalAvatarImage(
                                                model = avatarUrl,
                                                contentDescription = "Preset Avatar",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Option 1: Choose from Gallery / Storage
                        Surface(
                            onClick = {
                                showPhotoOptionsDialog = false
                                imagePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_gallery_photo_btn"),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(FirePrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = FirePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Upload from Gallery",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Choose an image from your device photos",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Option 2: Choose Preset Avatar
                        Surface(
                            onClick = { showPresets = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("choose_preset_avatar_btn"),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Pick Preset Avatar",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Select from cartoon character avatars",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Option 3: Image Web Link
                        Surface(
                            onClick = {
                                showPhotoOptionsDialog = false
                                showUrlInputDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("enter_photo_url_btn"),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Enter Image URL",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Paste a direct link to any image online",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Option 4: Remove Custom Photo (if set)
                        if (!currentUser?.profilePhotoUri.isNullOrEmpty()) {
                            Surface(
                                onClick = {
                                    onUpdateProfilePhoto(null)
                                    showPhotoOptionsDialog = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("remove_photo_btn"),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.errorContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Remove Custom Photo",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "Revert back to standard avatar",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (showPresets) {
                    TextButton(onClick = { showPresets = false }) {
                        Text("Back")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoOptionsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Photo Web URL Input Dialog
    if (showUrlInputDialog) {
        var tempUrl by remember { mutableStateOf(currentUser?.profilePhotoUri ?: "") }
        AlertDialog(
            onDismissRequest = { showUrlInputDialog = false },
            title = { Text("Enter Profile Photo URL") },
            text = {
                Column {
                    Text(
                        text = "Paste a direct web link to an image file (e.g. https://.../photo.jpg):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("Image URL") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("photo_url_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempUrl.isNotBlank()) {
                            onUpdateProfilePhoto(tempUrl.trim())
                        }
                        showUrlInputDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FirePrimary),
                    modifier = Modifier.testTag("save_photo_url_btn")
                ) {
                    Text("Apply Photo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlInputDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PersonalBestRow(
    title: String,
    value: String,
    icon: ImageVector,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FirePrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = FirePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FirePrimary
            )
        }

        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(OutlineVariant.copy(alpha = 0.4f))
            )
        }
    }
}
