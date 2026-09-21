package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraTextPrimary

/**
 * Premium gold/cyan badge highlighting the student's main academic target:
 * 90+ MARKS TARGET
 * Gujarat Board • Class 12 Commerce
 */
@Composable
fun NexoraTargetBadge(
    modifier: Modifier = Modifier,
    targetTitle: String = "90+ MARKS TARGET",
    targetText: String = "90+ MARKS",
    subtitle: String = "Gujarat Board • Class 12 Commerce",
    compact: Boolean = false
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            NexoraGold,
            NexoraCyan
        )
    )

    val backgroundBrush = Brush.horizontalGradient(
        colors = listOf(
            NexoraGold.copy(alpha = 0.16f),
            NexoraSurfaceElevated,
            NexoraCyan.copy(alpha = 0.16f)
        )
    )

    val titleDisplay = if (targetTitle.isNotEmpty()) {
        targetTitle
    } else {
        if (targetText.contains("TARGET", ignoreCase = true)) targetText else "$targetText TARGET"
    }

    Box(
        modifier = modifier
            .background(backgroundBrush, shape = RoundedCornerShape(16.dp))
            .border(1.5.dp, gradientBrush, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = if (compact) 12.dp else 16.dp, vertical = if (compact) 8.dp else 12.dp)
            .testTag("target_90_plus_badge"),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 32.dp else 40.dp)
                    .background(NexoraGold.copy(alpha = 0.22f), shape = RoundedCornerShape(10.dp))
                    .border(1.dp, NexoraGold.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Stars,
                    contentDescription = "Target 90+ Marks",
                    tint = NexoraGold,
                    modifier = Modifier.size(if (compact) 18.dp else 22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleDisplay,
                    color = NexoraGold,
                    fontSize = if (compact) 13.sp else 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = NexoraCyan,
                        fontSize = if (compact) 11.sp else 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(NexoraGold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "TARGET",
                    color = NexoraGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}
