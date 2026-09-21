package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraElectricBlue
import com.example.ui.theme.NexoraMagenta
import com.example.ui.theme.NexoraPink
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary

/**
 * Official NEXORA LEARN Logo Component.
 * Faithfully renders the emblem and typography from the official branding asset.
 */
@Composable
fun NexoraLogo(
    modifier: Modifier = Modifier,
    emblemSize: Dp = 140.dp,
    showWordmark: Boolean = true,
    showSubtitle: Boolean = true,
    subtitleText: String = "Smart Study. Better Preparation."
) {
    Column(
        modifier = modifier.testTag("nexora_logo_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // High-Precision Vector Canvas for the 3D N Emblem
        Canvas(
            modifier = Modifier
                .size(emblemSize)
                .testTag("nexora_emblem_canvas")
        ) {
            drawNexoraEmblem()
        }

        if (showWordmark) {
            Spacer(modifier = Modifier.height(14.dp))

            // Brand Title "NEXORA"
            Text(
                text = "NEXORA",
                color = NexoraTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 4.sp,
                modifier = Modifier.testTag("nexora_title_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Sub-branding "— LEARN —"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.testTag("nexora_learn_row")
            ) {
                Text(
                    text = "— ",
                    color = NexoraMagenta,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "LEARN",
                    color = NexoraCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 5.sp
                )
                Text(
                    text = " —",
                    color = NexoraMagenta,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showSubtitle) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = subtitleText,
                color = NexoraTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.4.sp,
                modifier = Modifier.testTag("nexora_subtitle_text")
            )
        }
    }
}

/**
 * Draws the exact NEXORA emblem geometry:
 * - Ambient radial glow
 * - Speech bubble with dots on left
 * - 3D N with Cyan-to-Purple-to-Magenta gradient
 * - Planetary orbit ring with 4-point star sparkle
 * - AI silhouette head with neural nodes & sound waves on right
 */
private fun DrawScope.drawNexoraEmblem() {
    val w = size.width
    val h = size.height
    val scale = w / 200f // normalized against 200x200 canvas

    // 1. Ambient Glow behind the N
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x3500E5FF),
                Color(0x22A855F7),
                Color(0x00000000)
            ),
            center = Offset(100f * scale, 95f * scale),
            radius = 75f * scale
        )
    )

    // 2. Planetary Orbit Ring (Rear Loop - purple/magenta)
    val rearRingPath = Path().apply {
        moveTo(125f * scale, 65f * scale)
        cubicTo(
            145f * scale, 50f * scale,
            155f * scale, 62f * scale,
            156f * scale, 75f * scale
        )
        cubicTo(
            157f * scale, 88f * scale,
            140f * scale, 110f * scale,
            115f * scale, 126f * scale
        )
    }
    drawPath(
        path = rearRingPath,
        color = NexoraMagenta,
        style = Stroke(width = 3.2f * scale, cap = StrokeCap.Round)
    )

    // 3. 3D Letter 'N' Geometry
    val nLeftX = 74f * scale
    val nRightX = 114f * scale
    val nWidth = 16f * scale
    val nTopY = 56f * scale
    val nBottomY = 136f * scale
    val nCorner = 4f * scale

    // 3a. Left Vertical Stem (Cyan to Electric Blue)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(NexoraCyan, NexoraElectricBlue),
            startY = nTopY,
            endY = nBottomY
        ),
        topLeft = Offset(nLeftX, nTopY),
        size = Size(nWidth, nBottomY - nTopY),
        cornerRadius = CornerRadius(nCorner, nCorner)
    )

    // 3b. Diagonal Stem (Electric Blue to Magenta)
    val diagonalPath = Path().apply {
        moveTo(nLeftX, nTopY + 2f * scale)
        lineTo(nLeftX + nWidth, nTopY)
        lineTo(nRightX + nWidth, nBottomY - 2f * scale)
        lineTo(nRightX, nBottomY)
        close()
    }
    drawPath(
        path = diagonalPath,
        brush = Brush.linearGradient(
            colors = listOf(NexoraElectricBlue, NexoraPurple, NexoraMagenta),
            start = Offset(nLeftX, nTopY),
            end = Offset(nRightX + nWidth, nBottomY)
        )
    )

    // 3c. Right Vertical Stem (Magenta to Violet Pink)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(NexoraPink, NexoraMagenta),
            startY = nTopY,
            endY = nBottomY
        ),
        topLeft = Offset(nRightX, nTopY),
        size = Size(nWidth, nBottomY - nTopY),
        cornerRadius = CornerRadius(nCorner, nCorner)
    )

    // 4. Speech Bubble on Left (Cyan)
    val bubbleX = 35f * scale
    val bubbleY = 76f * scale
    val bubbleW = 26f * scale
    val bubbleH = 18f * scale
    drawRoundRect(
        color = NexoraCyan,
        topLeft = Offset(bubbleX, bubbleY),
        size = Size(bubbleW, bubbleH),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )
    // Speech bubble tail
    val bubbleTail = Path().apply {
        moveTo(bubbleX + 6f * scale, bubbleY + bubbleH)
        lineTo(bubbleX + 16f * scale, bubbleY + bubbleH)
        lineTo(bubbleX + 22f * scale, bubbleY + bubbleH + 7f * scale)
        close()
    }
    drawPath(path = bubbleTail, color = NexoraCyan)

    // Three white dots inside speech bubble
    val dotY = bubbleY + bubbleH / 2f
    val dotR = 1.6f * scale
    drawCircle(Color.White, radius = dotR, center = Offset(bubbleX + 7f * scale, dotY))
    drawCircle(Color.White, radius = dotR, center = Offset(bubbleX + 13f * scale, dotY))
    drawCircle(Color.White, radius = dotR, center = Offset(bubbleX + 19f * scale, dotY))

    // 5. Planetary Orbit Ring (Front Swoop across N)
    val frontRingPath = Path().apply {
        moveTo(44f * scale, 102f * scale)
        cubicTo(
            52f * scale, 126f * scale,
            80f * scale, 138f * scale,
            110f * scale, 128f * scale
        )
        cubicTo(
            132f * scale, 120f * scale,
            145f * scale, 106f * scale,
            148f * scale, 94f * scale
        )
    }
    drawPath(
        path = frontRingPath,
        brush = Brush.horizontalGradient(
            colors = listOf(NexoraCyan, NexoraElectricBlue, NexoraPurple, NexoraMagenta),
            startX = 44f * scale,
            endX = 148f * scale
        ),
        style = Stroke(width = 3.6f * scale, cap = StrokeCap.Round)
    )

    // 6. 4-point Sparkle Star at the upper-right orbit crest
    val starCenterX = 149f * scale
    val starCenterY = 56f * scale
    val starArm = 8f * scale
    val starPath = Path().apply {
        moveTo(starCenterX, starCenterY - starArm)
        quadraticTo(starCenterX, starCenterY, starCenterX + starArm, starCenterY)
        quadraticTo(starCenterX, starCenterY, starCenterX, starCenterY + starArm)
        quadraticTo(starCenterX, starCenterY, starCenterX - starArm, starCenterY)
        quadraticTo(starCenterX, starCenterY, starCenterX, starCenterY - starArm)
        close()
    }
    drawPath(path = starPath, color = Color.White)
    drawCircle(
        color = NexoraCyan.copy(alpha = 0.5f),
        radius = 4f * scale,
        center = Offset(starCenterX, starCenterY)
    )

    // 7. Human AI Silhouette Profile on the Right
    val headPath = Path().apply {
        moveTo(150f * scale, 76f * scale)
        cubicTo(
            156f * scale, 74f * scale,
            164f * scale, 76f * scale,
            165f * scale, 83f * scale
        )
        cubicTo(
            166f * scale, 88f * scale,
            162f * scale, 92f * scale,
            164f * scale, 97f * scale
        )
        // Nose & mouth indent
        cubicTo(
            166f * scale, 99f * scale,
            168f * scale, 101f * scale,
            167f * scale, 105f * scale
        )
        cubicTo(
            166f * scale, 110f * scale,
            160f * scale, 116f * scale,
            152f * scale, 120f * scale
        )
    }
    drawPath(
        path = headPath,
        color = NexoraPurple,
        style = Stroke(width = 2.2f * scale, cap = StrokeCap.Round)
    )

    // 8. AI Brain Neural Constellation inside head
    val n1 = Offset(152f * scale, 84f * scale)
    val n2 = Offset(158f * scale, 88f * scale)
    val n3 = Offset(156f * scale, 96f * scale)
    val n4 = Offset(150f * scale, 98f * scale)
    val n5 = Offset(156f * scale, 106f * scale)

    // Node connector lines
    val netColor = NexoraCyan.copy(alpha = 0.7f)
    drawLine(netColor, n1, n2, strokeWidth = 1.2f * scale)
    drawLine(netColor, n2, n3, strokeWidth = 1.2f * scale)
    drawLine(netColor, n1, n4, strokeWidth = 1.2f * scale)
    drawLine(netColor, n4, n3, strokeWidth = 1.2f * scale)
    drawLine(netColor, n3, n5, strokeWidth = 1.2f * scale)

    // Neural node dots
    val nodeRadius = 2.2f * scale
    listOf(n1, n2, n3, n4, n5).forEach { node ->
        drawCircle(NexoraCyan, radius = nodeRadius, center = node)
    }

    // 9. Sound waves in front of mouth
    val wave1 = Path().apply {
        moveTo(172f * scale, 100f * scale)
        cubicTo(
            175f * scale, 103f * scale,
            175f * scale, 107f * scale,
            172f * scale, 110f * scale
        )
    }
    val wave2 = Path().apply {
        moveTo(177f * scale, 97f * scale)
        cubicTo(
            181f * scale, 102f * scale,
            181f * scale, 108f * scale,
            177f * scale, 113f * scale
        )
    }
    drawPath(wave1, color = NexoraMagenta, style = Stroke(width = 1.8f * scale, cap = StrokeCap.Round))
    drawPath(wave2, color = NexoraPink, style = Stroke(width = 1.8f * scale, cap = StrokeCap.Round))
}
