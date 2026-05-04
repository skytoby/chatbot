package com.deepseek.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.deepseek.chat.ui.theme.DeepSeekColors

/**
 * 聊天输入框组件
 */
@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // 输入框
        Box(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(DeepSeekColors.InputBackground)
                .border(1.dp, DeepSeekColors.Border, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = DeepSeekColors.OnSurface,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(DeepSeekColors.Primary),
                enabled = !isLoading,
                maxLines = 5,
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = "输入你的问题...",
                                fontSize = 16.sp,
                                color = DeepSeekColors.Muted
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 发送按钮
        SendButton(
            onClick = onSend,
            enabled = value.isNotBlank() && !isLoading,
            isLoading = isLoading
        )
    }
}

/**
 * 发送按钮
 */
@Composable
private fun SendButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (enabled) DeepSeekColors.Primary
                else DeepSeekColors.Muted
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = DeepSeekColors.Background,
                strokeWidth = 2.dp
            )
        } else {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "发送",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepSeekColors.Background
                )
            }
        }
    }
}
