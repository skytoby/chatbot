package com.deepseek.chat.ui.theme

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

@Composable
fun DeepSeekTheme(
    appColors: AppColors = ThemeStore.themes[0],
    content: @Composable () -> Unit
) {
    val colorScheme = if (appColors.isLight) {
        lightColorScheme(
            primary = appColors.Primary,
            secondary = appColors.PrimaryVariant,
            tertiary = appColors.Primary,
            background = appColors.Background,
            surface = appColors.Surface,
            surfaceVariant = appColors.SurfaceVariant,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = appColors.OnSurface,
            onSurface = appColors.OnSurface,
            onSurfaceVariant = appColors.OnSurfaceVariant,
            error = appColors.Error,
            outline = appColors.Border
        )
    } else {
        darkColorScheme(
            primary = appColors.Primary,
            secondary = appColors.PrimaryVariant,
            tertiary = appColors.Primary,
            background = appColors.Background,
            surface = appColors.Surface,
            surfaceVariant = appColors.SurfaceVariant,
            onPrimary = appColors.OnSurface,
            onSecondary = appColors.OnSurface,
            onTertiary = appColors.OnSurface,
            onBackground = appColors.OnSurface,
            onSurface = appColors.OnSurface,
            onSurfaceVariant = appColors.OnSurfaceVariant,
            error = appColors.Error,
            outline = appColors.Border
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as ComponentActivity
            val statusColor = appColors.Surface.toArgb()
            val navColor = appColors.Background.toArgb()
            if (appColors.isLight) {
                activity.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.light(statusColor, statusColor),
                    navigationBarStyle = SystemBarStyle.light(navColor, navColor)
                )
            } else {
                activity.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(statusColor),
                    navigationBarStyle = SystemBarStyle.dark(navColor)
                )
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
