package com.deepseek.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepseek.chat.ui.theme.DeepSeekColors
import com.deepseek.chat.ui.theme.ThemeStore

/**
 * 设置面板内容
 */
@Composable
fun SettingsPanel(
    currentThemeIndex: Int,
    onThemeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(DeepSeekColors.Surface)
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Text(
            text = "设置",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = DeepSeekColors.OnSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "主题颜色",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = DeepSeekColors.OnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        ThemeStore.themes.forEachIndexed { index, theme ->
            val isSelected = index == currentThemeIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) theme.Primary.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .clickable { onThemeSelected(index) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 颜色预览圆点
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(theme.Primary)
                        .then(
                            if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                            else Modifier
                        )
                )

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = theme.name,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) theme.Primary else DeepSeekColors.OnSurfaceVariant
                )
            }

            if (index < ThemeStore.themes.size - 1) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
