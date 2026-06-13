package com.aiagent.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Purple = Color(0xFF6C5CE7)
private val PurpleLight = Color(0xFFA29BFE)

private val DarkColors = darkColorScheme(
    primary = PurpleLight,
    onPrimary = Color(0xFF1A1430),
    background = Color(0xFF0B0B0F),
    surface = Color(0xFF15151D),
    surfaceVariant = Color(0xFF20202B),
)

private val LightColors = lightColorScheme(
    primary = Purple,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
