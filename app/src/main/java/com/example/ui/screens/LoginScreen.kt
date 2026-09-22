package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.constants.AppConstants
import com.example.core.model.StudentProfile
import com.example.core.repository.AuthRepository
import com.example.core.repository.AuthState
import com.example.ui.components.NexoraCard
import com.example.ui.components.NexoraLogo
import com.example.ui.components.NexoraPrimaryButton
import com.example.ui.components.NexoraSecondaryButton
import com.example.ui.components.NexoraTargetBadge
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraError
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraMagenta
import com.example.ui.theme.NexoraPink
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraSuccess
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import kotlinx.coroutines.launch

/**
 * Polished Login and Onboarding Screen for NEXORA LEARN.
 * Prepared for Google Sign-In and Mobile + OTP authentication,
 * capturing Student Name and confirming Gujarati Medium Class 12 Commerce.
 */
@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: (StudentProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by authRepository.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = context as? Activity

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Google, 1: Mobile + OTP
    var studentName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    // Check if successfully authenticated
    if (authState is AuthState.Authenticated) {
        onLoginSuccess((authState as AuthState.Authenticated).profile)
    }

    val isOtpSent = authState is AuthState.OtpSent
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NexoraBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("login_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Official Logo Branding
            NexoraLogo(
                emblemSize = 90.dp,
                showWordmark = true,
                showSubtitle = true,
                subtitleText = stringResource(R.string.app_subtitle)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Highlight 90+ MARKS Target with Premium Gold/Cyan Badge
            NexoraTargetBadge(
                modifier = Modifier.fillMaxWidth(),
                targetText = "90+ MARKS",
                subtitle = "GSEB Class 12 Commerce • Primary Target"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Gujarati Medium Confirmation (Strictly Gujarati Medium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexoraSurfaceVariant, shape = RoundedCornerShape(10.dp))
                    .border(1.dp, NexoraBorder, RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = NexoraCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Enrolled Stream: Gujarati Medium • Class 12 Commerce",
                        color = NexoraCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Auth Form Card
            NexoraCard(
                modifier = Modifier.testTag("auth_form_card"),
                contentPadding = 20.dp
            ) {
                // Tab Selection: Google Sign-In or Mobile OTP (only when OTP is not actively being verified)
                if (!isOtpSent) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = NexoraSurfaceVariant,
                        contentColor = NexoraCyan,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = NexoraCyan,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("auth_tab_row")
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                authRepository.clearError()
                            },
                            text = {
                                Text(
                                    text = "Google Sign-In",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) NexoraCyan else NexoraTextSecondary
                                )
                            },
                            modifier = Modifier.testTag("tab_google")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                authRepository.clearError()
                            },
                            text = {
                                Text(
                                    text = "Mobile + OTP",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) NexoraCyan else NexoraTextSecondary
                                )
                            },
                            modifier = Modifier.testTag("tab_mobile")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Error Banner with Actionable Guidance
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { error ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NexoraError.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .border(1.dp, NexoraError.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                                .testTag("error_banner")
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    tint = NexoraError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    color = NexoraTextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { authRepository.clearError() },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Dismiss error",
                                        tint = NexoraTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (selectedTab == 1 && (error.contains("Google Sign-In", ignoreCase = true) || error.contains("SMS", ignoreCase = true))) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        selectedTab = 0
                                        authRepository.clearError()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NexoraCyan.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = NexoraCyan.copy(alpha = 0.08f),
                                        contentColor = NexoraCyan
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = NexoraCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Switch to Google Sign-In",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                if (isOtpSent) {
                    // OTP Verification Step
                    val otpState = authState as AuthState.OtpSent
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { authRepository.clearError() },
                            modifier = Modifier.testTag("back_to_phone_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = NexoraCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verify OTP",
                            color = NexoraTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter the 6-digit code sent to +91 ${otpState.phoneNumber}",
                        color = NexoraTextMuted,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6) otpCode = it },
                        label = { Text("6-Digit OTP Code") },
                        placeholder = { Text("123456") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                scope.launch {
                                    authRepository.verifyPhoneOtp(
                                        verificationId = otpState.verificationId,
                                        phoneNumber = otpState.phoneNumber,
                                        otp = otpCode,
                                        studentName = otpState.studentName
                                    )
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexoraCyan,
                            unfocusedBorderColor = NexoraBorder,
                            focusedTextColor = NexoraTextPrimary,
                            unfocusedTextColor = NexoraTextPrimary,
                            focusedLabelColor = NexoraCyan,
                            unfocusedLabelColor = NexoraTextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_input_field")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    NexoraPrimaryButton(
                        text = "Confirm & Start Studying",
                        onClick = {
                            focusManager.clearFocus()
                            scope.launch {
                                authRepository.verifyPhoneOtp(
                                    verificationId = otpState.verificationId,
                                    phoneNumber = otpState.phoneNumber,
                                    otp = otpCode,
                                    studentName = otpState.studentName
                                )
                            }
                        },
                        isLoading = isLoading,
                        enabled = otpCode.length == 6,
                        testTag = "verify_otp_button"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    NexoraSecondaryButton(
                        text = "Resend OTP",
                        onClick = {
                            scope.launch {
                                authRepository.sendPhoneOtp(
                                    activity = activity,
                                    phoneNumber = otpState.phoneNumber,
                                    studentName = otpState.studentName
                                )
                            }
                        },
                        testTag = "resend_otp_button"
                    )
                } else {
                    // Standard Form: Student Name + Auth Mode
                    Text(
                        text = "Student Registration",
                        color = NexoraTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Confirm your details for Class 12 Commerce preparation.",
                        color = NexoraTextMuted,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Student Name Field
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Full Student Name") },
                        placeholder = { Text("e.g. Aarav Patel") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = NexoraCyan
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = if (selectedTab == 0) ImeAction.Done else ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexoraCyan,
                            unfocusedBorderColor = NexoraBorder,
                            focusedTextColor = NexoraTextPrimary,
                            unfocusedTextColor = NexoraTextPrimary,
                            focusedLabelColor = NexoraCyan,
                            unfocusedLabelColor = NexoraTextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_name_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedTab == 1) {
                        // Mobile Number Input
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 10) phoneNumber = it },
                            label = { Text("Mobile Number (10 digits)") },
                            placeholder = { Text("9876543210") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = null,
                                    tint = NexoraMagenta
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    scope.launch {
                                        authRepository.sendPhoneOtp(
                                            activity = activity,
                                            phoneNumber = phoneNumber,
                                            studentName = studentName
                                        )
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NexoraMagenta,
                                unfocusedBorderColor = NexoraBorder,
                                focusedTextColor = NexoraTextPrimary,
                                unfocusedTextColor = NexoraTextPrimary,
                                focusedLabelColor = NexoraMagenta,
                                unfocusedLabelColor = NexoraTextSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_number_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        NexoraPrimaryButton(
                            text = "Send Verification OTP",
                            onClick = {
                                focusManager.clearFocus()
                                scope.launch {
                                    authRepository.sendPhoneOtp(
                                        activity = activity,
                                        phoneNumber = phoneNumber,
                                        studentName = studentName
                                    )
                                }
                            },
                            isLoading = isLoading,
                            enabled = studentName.isNotBlank() && phoneNumber.length == 10,
                            testTag = "send_otp_button"
                        )
                    } else {
                        // Google Sign-In Action
                        Spacer(modifier = Modifier.height(10.dp))

                        NexoraPrimaryButton(
                            text = "Continue with Google",
                            onClick = {
                                focusManager.clearFocus()
                                scope.launch {
                                    authRepository.initiateGoogleSignIn(
                                        context = context,
                                        studentName = studentName
                                    )
                                }
                            },
                            isLoading = isLoading,
                            enabled = studentName.isNotBlank(),
                            testTag = "google_signin_button"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Class 12 Commerce Target Guarantee Note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = null,
                    tint = NexoraTextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GSEB Class 12 Commerce • Target 90+ Marks",
                    color = NexoraTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
