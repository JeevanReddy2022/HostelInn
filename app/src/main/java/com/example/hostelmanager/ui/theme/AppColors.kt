package com.example.hostelmanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Shared Brand & Status Colors
val AppNavy = Color(0xFF0B1F33)
val AppNavyLight = Color(0xFF123B5D)
val AppSky = Color(0xFF38BDF8)
val AppGreen = Color(0xFF10B981)
val AppOrange = Color(0xFFF97316)
val AppRed = Color(0xFFE11D48)

// Adaptive Theme Token Accessors
val AppBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val AppCard: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val AppText: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val AppMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val AppBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outlineVariant

val AppSoftBlue: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFF0C4A6E) else Color(0xFFE0F2FE)

val AppSoftGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFF064E3B) else Color(0xFFD1FAE5)

val AppSoftOrange: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFF7C2D12) else Color(0xFFFFEDD5)

val AppSoftRed: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFF881337) else Color(0xFFFFE4E6)
