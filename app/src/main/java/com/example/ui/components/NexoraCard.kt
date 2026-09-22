package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraSurface

/**
 * Reusable Card component adhering to the premium light aesthetic
 * with rounded corners, subtle border, and soft shadow.
 */
@Composable
fun NexoraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color = NexoraBorder,
    containerColor: Color = NexoraSurface,
    cornerRadius: Dp = 16.dp,
    contentPadding: Dp = 16.dp,
    testTag: String = "nexora_card",
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(cornerRadius),
            border = BorderStroke(1.dp, borderColor),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = modifier
                .fillMaxWidth()
                .testTag(testTag)
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    } else {
        Card(
            shape = RoundedCornerShape(cornerRadius),
            border = BorderStroke(1.dp, borderColor),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = modifier
                .fillMaxWidth()
                .testTag(testTag)
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}
