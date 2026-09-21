package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Background and Surface Palette (Premium Dark Theme)
val NexoraBackground = Color(0xFF060B1E)
val NexoraSurface = Color(0xFF0A122E)
val NexoraSurfaceVariant = Color(0xFF111D44)
val NexoraSurfaceElevated = Color(0xFF162557)
val NexoraBorder = Color(0xFF1E316B)
val NexoraBorderGlow = Color(0x5500E5FF)

// Brand Core Accents inspired by NEXORA LEARN Logo
val NexoraCyan = Color(0xFF00E5FF)
val NexoraElectricBlue = Color(0xFF0080FF)
val NexoraPurple = Color(0xFFA855F7)
val NexoraMagenta = Color(0xFFD946EF)
val NexoraPink = Color(0xFFEC4899)
val NexoraGold = Color(0xFFFFC107)

// Text Colors
val NexoraTextPrimary = Color(0xFFFFFFFF)
val NexoraTextSecondary = Color(0xFF94A3B8)
val NexoraTextMuted = Color(0xFF64748B)

// Status & Semantic Colors
val NexoraSuccess = Color(0xFF10B981)
val NexoraWarning = Color(0xFFF59E0B)
val NexoraError = Color(0xFFEF4444)

// Brand Gradients
val NexoraLogoGradient = Brush.linearGradient(
    colors = listOf(NexoraCyan, NexoraElectricBlue, NexoraPurple, NexoraMagenta)
)

val NexoraButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF), Color(0xFF9333EA))
)

val NexoraCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF132252), Color(0xFF0D1739))
)

val NexoraGlobeAtmosphere = Brush.verticalGradient(
    colors = listOf(Color(0x0000E5FF), Color(0x3300E5FF), Color(0x660072FF))
)

