package dev.watchbox.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val WatchBoxColorScheme = darkColorScheme(
    primary = Coral500,
    onPrimary = White,
    primaryContainer = Coral600,
    onPrimaryContainer = White,
    secondary = Grey400,
    onSecondary = Navy950,
    background = Navy950,
    onBackground = White,
    surface = Navy900,
    onSurface = White,
    surfaceVariant = Navy800,
    onSurfaceVariant = Grey300,
    error = Coral500,
    onError = White,
)

@Composable
fun WatchBoxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WatchBoxColorScheme,
        typography = WatchBoxTypography,
        content = content,
    )
}
