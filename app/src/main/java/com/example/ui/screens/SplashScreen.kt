package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.constants.AppConstants
import com.example.ui.components.NexoraGlobeBackground
import com.example.ui.components.NexoraLogo
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraMagenta
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraTextMuted
import kotlinx.coroutines.delay

/**
 * Premium Splash Screen for NEXORA LEARN.
 * Displays the exact logo, branding typography, subtle smooth animation,
 * loading indicator, and globe horizon.
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "splash_alpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.92f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "splash_scale"
    )

    // Subtle spinner rotation
    val infiniteTransition = rememberInfiniteTransition(label = "loading_rotation")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(AppConstants.SPLASH_DURATION_MS)
        onSplashComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NexoraBackground,
                        NexoraSurface,
                        NexoraBackground
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("splash_screen_root")
    ) {
        // Center Content: Official Logo + Typography
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alphaAnim)
                .scale(scaleAnim),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            NexoraLogo(
                emblemSize = 160.dp,
                showWordmark = true,
                showSubtitle = true,
                subtitleText = stringResource(R.string.app_subtitle)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator matching mockup
            CircularProgressIndicator(
                modifier = Modifier
                    .size(34.dp)
                    .testTag("splash_progress_indicator"),
                color = NexoraCyan,
                trackColor = NexoraSurface,
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // "— LOADING —" Text with subtle pulse
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .alpha(pulseAlpha)
                    .testTag("splash_loading_row")
            ) {
                Text(
                    text = "— ",
                    color = NexoraMagenta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.loading_text),
                    color = NexoraCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Text(
                    text = " —",
                    color = NexoraMagenta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom Decorative Element: Globe Horizon Atmosphere
        NexoraGlobeBackground(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .alpha(alphaAnim),
            height = 160.dp
        )
    }
}
