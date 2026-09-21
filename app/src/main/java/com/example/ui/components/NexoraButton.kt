package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraButtonGradient
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraTextPrimary

/**
 * Reusable primary button with brand gradient and minimum 48dp touch target.
 */
@Composable
fun NexoraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    testTag: String = "primary_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color(0x3300E5FF)
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .background(
                    brush = if (enabled && !isLoading) NexoraButtonGradient else NexoraButtonGradient,
                    shape = RoundedCornerShape(14.dp),
                    alpha = if (enabled && !isLoading) 1f else 0.5f
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = NexoraBackground,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.testTag("button_loading_indicator")
                )
            } else {
                Text(
                    text = text,
                    color = NexoraBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Reusable secondary outlined button with subtle glassmorphism border.
 */
@Composable
fun NexoraSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "secondary_button"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, NexoraBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = NexoraTextPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .testTag(testTag)
    ) {
        Text(
            text = text,
            color = if (enabled) NexoraTextPrimary else NexoraCyan.copy(alpha = 0.5f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
