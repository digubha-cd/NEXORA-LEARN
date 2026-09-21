package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraElectricBlue

/**
 * Renders the glowing planetary globe horizon at the bottom of the screen,
 * exactly as styled in the official splash screen mockup.
 */
@Composable
fun NexoraGlobeBackground(
    modifier: Modifier = Modifier,
    height: Dp = 180.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height

        // Globe sphere arc at bottom center
        val centerX = w / 2f
        val centerY = h + 180f
        val radius = w * 0.78f

        // Atmospheric rim glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NexoraCyan.copy(alpha = 0.45f),
                    NexoraElectricBlue.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = Offset(centerX, h * 0.4f),
                radius = radius * 0.8f
            )
        )

        // Dotted matrix / longitude latitude arcs
        val arcPath = Path().apply {
            moveTo(0f, h * 0.65f)
            quadraticTo(centerX, h * 0.35f, w, h * 0.65f)
        }
        drawPath(
            path = arcPath,
            color = NexoraCyan.copy(alpha = 0.8f),
            style = Stroke(width = 2.5f)
        )

        val secondArc = Path().apply {
            moveTo(0f, h * 0.85f)
            quadraticTo(centerX, h * 0.58f, w, h * 0.85f)
        }
        drawPath(
            path = secondArc,
            color = NexoraCyan.copy(alpha = 0.4f),
            style = Stroke(width = 1.5f)
        )

        // Dotted grid texture representing global connectivity & intelligence
        val stepX = w / 16f
        for (i in 1..15) {
            val px = i * stepX
            val curveFactor = 1f - kotlin.math.abs(px - centerX) / (w * 0.5f)
            if (curveFactor > 0f) {
                val py = h * 0.45f + (1f - curveFactor) * (h * 0.35f)
                drawCircle(
                    color = NexoraCyan.copy(alpha = 0.35f * curveFactor),
                    radius = 1.8f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = NexoraCyan.copy(alpha = 0.25f * curveFactor),
                    radius = 1.4f,
                    center = Offset(px, py + 24f)
                )
            }
        }
    }
}
