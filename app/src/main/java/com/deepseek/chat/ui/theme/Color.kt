package com.deepseek.chat.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

/**
 * 主题配色方案
 */
data class AppColors(
    val name: String,
    val Background: Color,
    val Surface: Color,
    val SurfaceVariant: Color,
    val Primary: Color,
    val PrimaryVariant: Color,
    val OnSurface: Color,
    val OnSurfaceVariant: Color,
    val Muted: Color,
    val Border: Color,
    val InputBackground: Color,
    val Error: Color,
    val UserBubble: Color,
    val AssistantBubble: Color,
    val isLight: Boolean = false
)

/**
 * 6 种主题
 */
object ThemeStore {
    val themes = listOf(
        // 1. 默认深蓝 (原主题)
        AppColors(
            name = "深邃蓝",
            Background = Color(0xFF000000),
            Surface = Color(0xFF0D0D0D),
            SurfaceVariant = Color(0xFF1A1A1A),
            Primary = Color(0xFF60A5FA),
            PrimaryVariant = Color(0xFF3B82F6),
            OnSurface = Color(0xFFFFFFFF),
            OnSurfaceVariant = Color(0xFFE5E5E5),
            Muted = Color(0xFF666666),
            Border = Color(0xFF1A1A1A),
            InputBackground = Color(0xFF1A1A1A),
            Error = Color(0xFFEF4444),
            UserBubble = Color(0xFF60A5FA),
            AssistantBubble = Color(0xFF1A1A1A)
        ),
        // 2. 翡翠绿
        AppColors(
            name = "翡翠绿",
            Background = Color(0xFF0A0F0D),
            Surface = Color(0xFF0F1A15),
            SurfaceVariant = Color(0xFF162420),
            Primary = Color(0xFF34D399),
            PrimaryVariant = Color(0xFF10B981),
            OnSurface = Color(0xFFFFFFFF),
            OnSurfaceVariant = Color(0xFFD1FAE5),
            Muted = Color(0xFF5F7A6E),
            Border = Color(0xFF1C2E26),
            InputBackground = Color(0xFF162420),
            Error = Color(0xFFEF4444),
            UserBubble = Color(0xFF34D399),
            AssistantBubble = Color(0xFF162420)
        ),
        // 3. 暖橙
        AppColors(
            name = "暖阳橙",
            Background = Color(0xFF0F0A07),
            Surface = Color(0xFF1A120C),
            SurfaceVariant = Color(0xFF241A12),
            Primary = Color(0xFFFB923C),
            PrimaryVariant = Color(0xFFF97316),
            OnSurface = Color(0xFFFFFFFF),
            OnSurfaceVariant = Color(0xFFFED7AA),
            Muted = Color(0xFF7A6A5A),
            Border = Color(0xFF2E2218),
            InputBackground = Color(0xFF241A12),
            Error = Color(0xFFEF4444),
            UserBubble = Color(0xFFFB923C),
            AssistantBubble = Color(0xFF241A12)
        ),
        // 4. 玫瑰粉
        AppColors(
            name = "玫瑰粉",
            Background = Color(0xFF0F0A0C),
            Surface = Color(0xFF1A0F14),
            SurfaceVariant = Color(0xFF24161D),
            Primary = Color(0xFFF472B6),
            PrimaryVariant = Color(0xFFEC4899),
            OnSurface = Color(0xFFFFFFFF),
            OnSurfaceVariant = Color(0xFFFBCFE8),
            Muted = Color(0xFF7A5F6E),
            Border = Color(0xFF2E1C26),
            InputBackground = Color(0xFF24161D),
            Error = Color(0xFFEF4444),
            UserBubble = Color(0xFFF472B6),
            AssistantBubble = Color(0xFF24161D)
        ),
        // 5. 星空紫
        AppColors(
            name = "星空紫",
            Background = Color(0xFF0A0A10),
            Surface = Color(0xFF0F0F1A),
            SurfaceVariant = Color(0xFF181824),
            Primary = Color(0xFFA78BFA),
            PrimaryVariant = Color(0xFF8B5CF6),
            OnSurface = Color(0xFFFFFFFF),
            OnSurfaceVariant = Color(0xFFDDD6FE),
            Muted = Color(0xFF6B6080),
            Border = Color(0xFF22203A),
            InputBackground = Color(0xFF181824),
            Error = Color(0xFFEF4444),
            UserBubble = Color(0xFFA78BFA),
            AssistantBubble = Color(0xFF181824)
        ),
        // 6. 琥珀金
        AppColors(
            name = "琥珀金",
            Background = Color(0xFF0F0D07),
            Surface = Color(0xFF1A160C),
            SurfaceVariant = Color(0xFF242012),
            Primary = Color(0xFFFBBF24),
            PrimaryVariant = Color(0xFFF59E0B),
            OnSurface = Color(0xFFFFFFFF),
            OnSurfaceVariant = Color(0xFFFEF3C7),
            Muted = Color(0xFF7A7050),
            Border = Color(0xFF2E2A18),
            InputBackground = Color(0xFF242012),
            Error = Color(0xFFEF4444),
            UserBubble = Color(0xFFFBBF24),
            AssistantBubble = Color(0xFF242012)
        ),
        // 7. 简约白
        AppColors(
            name = "简约白",
            Background = Color(0xFFF7F7F8),
            Surface = Color(0xFFFFFFFF),
            SurfaceVariant = Color(0xFFF0F0F0),
            Primary = Color(0xFF1A1A1A),
            PrimaryVariant = Color(0xFF333333),
            OnSurface = Color(0xFF1A1A1A),
            OnSurfaceVariant = Color(0xFF333333),
            Muted = Color(0xFF999999),
            Border = Color(0xFFE5E5E5),
            InputBackground = Color(0xFFF0F0F0),
            Error = Color(0xFFDC2626),
            UserBubble = Color(0xFF1A1A1A),
            AssistantBubble = Color(0xFFF0F0F0),
            isLight = true
        )
    )
}

/**
 * 当前主题的 CompositionLocal
 */
val LocalAppColors = compositionLocalOf { ThemeStore.themes[0] }

/**
 * 全局访问当前主题色（兼容旧代码中的 DeepSeekColors.XXX 写法）
 */
object DeepSeekColors {
    val Background: Color @Composable get() = LocalAppColors.current.Background
    val Surface: Color @Composable get() = LocalAppColors.current.Surface
    val SurfaceVariant: Color @Composable get() = LocalAppColors.current.SurfaceVariant
    val Primary: Color @Composable get() = LocalAppColors.current.Primary
    val PrimaryVariant: Color @Composable get() = LocalAppColors.current.PrimaryVariant
    val OnSurface: Color @Composable get() = LocalAppColors.current.OnSurface
    val OnSurfaceVariant: Color @Composable get() = LocalAppColors.current.OnSurfaceVariant
    val Muted: Color @Composable get() = LocalAppColors.current.Muted
    val Border: Color @Composable get() = LocalAppColors.current.Border
    val InputBackground: Color @Composable get() = LocalAppColors.current.InputBackground
    val Error: Color @Composable get() = LocalAppColors.current.Error
    val UserBubble: Color @Composable get() = LocalAppColors.current.UserBubble
    val AssistantBubble: Color @Composable get() = LocalAppColors.current.AssistantBubble
}
