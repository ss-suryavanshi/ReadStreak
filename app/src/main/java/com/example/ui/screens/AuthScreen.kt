package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.FirePrimary
import com.example.ui.theme.FirePrimaryContainer
import com.example.ui.theme.OnSuccessGreenContainer
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.SuccessGreenContainer

/**
 * Custom kinetic press modifier that scales down on touch for glassmorphism tactile feel.
 */
@Composable
fun Modifier.kineticPress(onClick: (() -> Unit)? = null): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "kinetic_scale"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )
}

@Composable
fun AuthScreen(
    errorMessage: String? = null,
    initialIsSignUp: Boolean = true,
    onLogin: (emailOrUsername: String, passwordRaw: String) -> Unit,
    onRegister: (username: String, email: String, passwordRaw: String, displayName: String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var selectedTabIndex by remember { mutableIntStateOf(if (initialIsSignUp) 0 else 1) } // 0 = Create Account, 1 = Log In

    // Input States
    var emailOrUsername by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regDisplayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(true) }

    val glassBackground = Color(0xFFFCF9F8)
    val glassCardSurface = Color.White.copy(alpha = 0.45f)
    val glassBorderColor = Color.White.copy(alpha = 0.65f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(glassBackground)
    ) {
        // --- Mesh Gradient Radial Blobs (Zero GPU Blur Overhead) ---
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-60).dp, y = 80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFFF6B35).copy(alpha = 0.20f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF00677E).copy(alpha = 0.14f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFAB3500).copy(alpha = 0.16f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        // Main Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Glassmorphic Top Navigation Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                color = Color.White.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .testTag("auth_back_button")
                                .kineticPress()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = FirePrimary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Text(
                        text = "Read Streak",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = FirePrimary,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                // Identity Branding Icon Badge
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(1.5.dp, FirePrimary.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.read_streak_logo_1785676270425),
                        contentDescription = "Read Streak Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (selectedTabIndex == 0) "Create Account" else "Welcome back",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (selectedTabIndex == 0)
                        "Fuel your reading momentum and start your daily streak today."
                    else
                        "Log in to keep your streak alive.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Error Banner
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { msg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                            )
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Glass Container Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = glassCardSurface
                    ),
                    border = BorderStroke(1.dp, glassBorderColor)
                ) {
                    Column {
                        // Switcher Tabs
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color.White.copy(alpha = 0.25f),
                            contentColor = FirePrimary,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = FirePrimary
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                text = {
                                    Text(
                                        text = "Create Account",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                modifier = Modifier.testTag("tab_register")
                            )
                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                text = {
                                    Text(
                                        text = "Log In",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                modifier = Modifier.testTag("tab_sign_in")
                            )
                        }

                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (selectedTabIndex == 0) {
                                // --- SIGN UP FORM ---
                                GlassInputField(
                                    value = regDisplayName,
                                    onValueChange = { regDisplayName = it },
                                    label = "FULL NAME",
                                    placeholder = "Jane Doe",
                                    leadingIcon = Icons.Default.Badge,
                                    testTag = "auth_reg_display_name"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                GlassInputField(
                                    value = regUsername,
                                    onValueChange = { regUsername = it },
                                    label = "USERNAME",
                                    placeholder = "janedoe",
                                    leadingIcon = Icons.Default.Person,
                                    testTag = "auth_reg_username"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                GlassInputField(
                                    value = regEmail,
                                    onValueChange = { regEmail = it },
                                    label = "EMAIL ADDRESS",
                                    placeholder = "jane@example.com",
                                    leadingIcon = Icons.Default.Email,
                                    keyboardType = KeyboardType.Email,
                                    testTag = "auth_reg_email"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                GlassInputField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = "PASSWORD",
                                    placeholder = "••••••••",
                                    leadingIcon = Icons.Default.Key,
                                    isPassword = true,
                                    isPasswordVisible = isPasswordVisible,
                                    onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                                    keyboardType = KeyboardType.Password,
                                    testTag = "auth_reg_password"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Terms Checkbox
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { agreeTerms = !agreeTerms },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = agreeTerms,
                                        onCheckedChange = { agreeTerms = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = FirePrimary,
                                            checkmarkColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("auth_terms_checkbox")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "I agree to the Terms of Service and Privacy Policy.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Massive Action Button: Start My Streak
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .kineticPress {
                                            if (agreeTerms) {
                                                val uName = if (regUsername.isBlank()) {
                                                    regEmail.substringBefore("@").ifBlank { "user" }
                                                } else regUsername
                                                onRegister(
                                                    uName,
                                                    regEmail,
                                                    password,
                                                    if (regDisplayName.isBlank()) uName else regDisplayName
                                                )
                                            }
                                        }
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (agreeTerms) FirePrimaryContainer else FirePrimaryContainer.copy(alpha = 0.5f))
                                        .testTag("auth_btn_register"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Start My Streak",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color(0xFF5F1900)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color(0xFF5F1900),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            } else {
                                // --- LOGIN FORM ---
                                GlassInputField(
                                    value = emailOrUsername,
                                    onValueChange = { emailOrUsername = it },
                                    label = "EMAIL OR USERNAME",
                                    placeholder = "hello@readstreak.app",
                                    leadingIcon = Icons.Default.Email,
                                    keyboardType = KeyboardType.Email,
                                    testTag = "auth_input_email"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                GlassInputField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = "PASSWORD",
                                    placeholder = "••••••••",
                                    leadingIcon = Icons.Default.Key,
                                    isPassword = true,
                                    isPasswordVisible = isPasswordVisible,
                                    onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                                    keyboardType = KeyboardType.Password,
                                    testTag = "auth_input_password"
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FirePrimary,
                                        modifier = Modifier
                                            .kineticPress { }
                                            .padding(vertical = 4.dp, horizontal = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Massive Action Button: Log In
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .kineticPress {
                                            onLogin(emailOrUsername, password)
                                        }
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(FirePrimary)
                                        .testTag("auth_btn_login"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Log In",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Security & Account Protection Note Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SuccessGreenContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, SuccessGreenContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Protected",
                            tint = OnSuccessGreenContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "End-to-End Account Protection",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSuccessGreenContainer
                            )
                            Text(
                                text = "Your credentials and reading progress are securely encrypted.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = OnSuccessGreenContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Secondary Navigation Footer
                Row(
                    modifier = Modifier
                        .kineticPress {
                            selectedTabIndex = if (selectedTabIndex == 0) 1 else 0
                        }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedTabIndex == 0) "Already have an account? " else "Don't have an account? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (selectedTabIndex == 0) "Log In" else "Sign Up",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = FirePrimary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun GlassInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            trailingIcon = if (isPassword && onTogglePasswordVisibility != null) {
                {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.55f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.35f),
                focusedBorderColor = FirePrimary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}


