package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Background and Surface Palette (Premium White / Light Theme)
val NexoraBackground = Color(0xFFF8FAFC)        // Clean, bright off-white / light slate canvas
val NexoraSurface = Color(0xFFFFFFFF)           // Pure crisp white card surface
val NexoraSurfaceVariant = Color(0xFFF1F5F9)    // Soft subtle light grey/blue container
val NexoraSurfaceElevated = Color(0xFFFFFFFF)   // Pure white elevated card
val NexoraBorder = Color(0xFFE2E8F0)            // Subtle, elegant light border
val NexoraBorderGlow = Color(0x330284C7)        // Soft azure highlight border

// Brand Core Accents (Adjusted for high contrast and vibrance on white/light backgrounds)
val NexoraCyan = Color(0xFF0284C7)              // Premium deep cyan / sky blue (contrast-safe on white)
val NexoraElectricBlue = Color(0xFF2563EB)      // Vibrant royal blue
val NexoraPurple = Color(0xFF7C3AED)            // Vivid violet purple
val NexoraMagenta = Color(0xFFC026D3)           // Deep rich magenta
val NexoraPink = Color(0xFFDB2777)              // Elegant deep rose pink
val NexoraGold = Color(0xFFD97706)              // Rich amber gold (contrast-safe on white)

// Text Colors (High-contrast dark navy / slate palette)
val NexoraTextPrimary = Color(0xFF0F172A)        // Deep navy/slate for crystal-clear readability
val NexoraTextSecondary = Color(0xFF475569)      // Balanced medium slate for secondary text
val NexoraTextMuted = Color(0xFF64748B)          // Refined muted slate for timestamps/captions

// Status & Semantic Colors
val NexoraSuccess = Color(0xFF059669)           // Crisp emerald green
val NexoraWarning = Color(0xFFD97706)           // Warm amber
val NexoraError = Color(0xFFDC2626)             // Clean ruby red

// Brand Gradients
val NexoraLogoGradient = Brush.linearGradient(
    colors = listOf(NexoraCyan, NexoraElectricBlue, NexoraPurple, NexoraMagenta)
)

val NexoraButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF0284C7), Color(0xFF2563EB), Color(0xFF7C3AED))
)

val NexoraCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC))
)

val NexoraGlobeAtmosphere = Brush.verticalGradient(
    colors = listOf(Color(0x000284C7), Color(0x180284C7), Color(0x302563EB))
)

