package com.deepseek.chat.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.deepseek.chat.domain.model.ChatMessage
import com.deepseek.chat.domain.model.MessageRole
import com.deepseek.chat.ui.theme.DeepSeekColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 消息气泡组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    onLongClick: (() -> Unit)? = null,
    typingIndicator: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) DeepSeekColors.UserBubble else DeepSeekColors.AssistantBubble
    val textColor = if (isUser) Color.White else DeepSeekColors.OnSurfaceVariant
    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(bubbleColor)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when {
                typingIndicator -> {
                    TypingIndicator()
                }
                isUser -> {
                    Text(
                        text = message.content,
                        color = textColor,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                }
                else -> {
                    MarkdownText(
                        markdown = message.content,
                        textColor = textColor
                    )
                }
            }
        }
    }
}

/**
 * Markdown 渲染组件 - 支持常见 Markdown 语法
 */
@Composable
fun MarkdownText(
    markdown: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val codeBackground = DeepSeekColors.Surface.copy(alpha = 0.5f)

    Column(modifier = modifier) {
        val blocks = parseMarkdownBlocks(markdown)
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MarkdownBlock.CodeBlock -> {
                    if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                    // 代码块语言标签
                    if (block.language.isNotEmpty()) {
                        Text(
                            text = block.language,
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(codeBackground)
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = block.code,
                            color = textColor,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (index < blocks.size - 1) Spacer(modifier = Modifier.height(8.dp))
                }
                is MarkdownBlock.Heading -> {
                    if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = block.text,
                        color = textColor,
                        fontSize = when (block.level) {
                            1 -> 22.sp
                            2 -> 20.sp
                            3 -> 18.sp
                            else -> 16.sp
                        },
                        fontWeight = FontWeight.Bold,
                        lineHeight = when (block.level) {
                            1 -> 28.sp
                            2 -> 26.sp
                            3 -> 24.sp
                            else -> 22.sp
                        }
                    )
                    if (index < blocks.size - 1) Spacer(modifier = Modifier.height(4.dp))
                }
                is MarkdownBlock.ListItem -> {
                    if (index > 0 && blocks[index - 1] !is MarkdownBlock.ListItem) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (block.ordered) "${block.number}. " else "•  ",
                            color = textColor,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                        Text(
                            text = buildInlineAnnotatedString(block.text, textColor, codeBackground),
                            modifier = Modifier.weight(1f),
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                    }
                }
                is MarkdownBlock.HorizontalRule -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = textColor.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                is MarkdownBlock.Paragraph -> {
                    if (index > 0) Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = buildInlineAnnotatedString(block.text, textColor, codeBackground),
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

/**
 * 解析 Markdown 内联样式（粗体、斜体、行内代码、删除线、链接）
 */
@Composable
private fun buildInlineAnnotatedString(
    text: String,
    textColor: Color,
    codeBackground: Color
) = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            // 行内代码 `code`
            text[i] == '`' && i + 1 < text.length -> {
                val end = text.indexOf('`', i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = codeBackground,
                        color = textColor,
                        fontSize = 14.sp
                    )) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    withStyle(SpanStyle(color = textColor)) { append(text[i]) }
                    i++
                }
            }
            // 粗体+斜体 ***text***
            text.startsWith("***", i) -> {
                val end = text.indexOf("***", i + 3)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = textColor)) {
                        append(text.substring(i + 3, end))
                    }
                    i = end + 3
                } else {
                    withStyle(SpanStyle(color = textColor)) { append(text[i]) }
                    i++
                }
            }
            // 粗体 **text**
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    withStyle(SpanStyle(color = textColor)) { append(text[i]) }
                    i++
                }
            }
            // 斜体 *text*
            text[i] == '*' && i + 1 < text.length && text[i + 1] != ' ' -> {
                val end = text.indexOf('*', i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = textColor)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    withStyle(SpanStyle(color = textColor)) { append(text[i]) }
                    i++
                }
            }
            // 删除线 ~~text~~
            text.startsWith("~~", i) -> {
                val end = text.indexOf("~~", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = textColor)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    withStyle(SpanStyle(color = textColor)) { append(text[i]) }
                    i++
                }
            }
            // 链接 [text](url)
            text[i] == '[' -> {
                val closeBracket = text.indexOf(']', i + 1)
                if (closeBracket != -1 && closeBracket + 1 < text.length && text[closeBracket + 1] == '(') {
                    val closeParen = text.indexOf(')', closeBracket + 2)
                    if (closeParen != -1) {
                        val linkText = text.substring(i + 1, closeBracket)
                        withStyle(SpanStyle(
                            color = Color(0xFF64B5F6),
                            textDecoration = TextDecoration.Underline
                        )) {
                            append(linkText)
                        }
                        i = closeParen + 1
                    } else {
                        withStyle(SpanStyle(color = textColor)) { append(text[i]) }
                        i++
                    }
                } else {
                    withStyle(SpanStyle(color = textColor)) { append(text[i]) }
                    i++
                }
            }
            else -> {
                withStyle(SpanStyle(color = textColor)) { append(text[i]) }
                i++
            }
        }
    }
}

/** Markdown 块类型 */
private sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class CodeBlock(val code: String, val language: String) : MarkdownBlock()
    data class Heading(val text: String, val level: Int) : MarkdownBlock()
    data class ListItem(val text: String, val ordered: Boolean, val number: Int = 0) : MarkdownBlock()
    object HorizontalRule : MarkdownBlock()
}

/** 将 Markdown 文本解析为块级元素 */
private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trimStart()

        when {
            // 代码块 ```
            trimmed.startsWith("```") -> {
                val language = trimmed.removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                blocks.add(MarkdownBlock.CodeBlock(codeLines.joinToString("\n"), language))
                i++ // skip closing ```
            }
            // 标题 #
            trimmed.startsWith("#") -> {
                val level = trimmed.takeWhile { it == '#' }.length.coerceAtMost(6)
                val text = trimmed.drop(level).trimStart()
                blocks.add(MarkdownBlock.Heading(text, level))
                i++
            }
            // 无序列表 - / * / +
            trimmed.matches(Regex("^[-*+]\\s+.*")) -> {
                val text = trimmed.replaceFirst(Regex("^[-*+]\\s+"), "")
                blocks.add(MarkdownBlock.ListItem(text, ordered = false))
                i++
            }
            // 有序列表 1.
            trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                val num = trimmed.takeWhile { it.isDigit() }.toIntOrNull() ?: 1
                val text = trimmed.replaceFirst(Regex("^\\d+\\.\\s+"), "")
                blocks.add(MarkdownBlock.ListItem(text, ordered = true, number = num))
                i++
            }
            // 水平线 ---/***/___ (3+个)
            trimmed.matches(Regex("^[-*_]{3,}\\s*$")) -> {
                blocks.add(MarkdownBlock.HorizontalRule)
                i++
            }
            // 空行 - 跳过
            trimmed.isEmpty() -> {
                i++
            }
            // 普通段落
            else -> {
                val paragraphLines = mutableListOf(line)
                i++
                while (i < lines.size) {
                    val nextTrimmed = lines[i].trimStart()
                    if (nextTrimmed.isEmpty() ||
                        nextTrimmed.startsWith("```") ||
                        nextTrimmed.startsWith("#") ||
                        nextTrimmed.matches(Regex("^[-*+]\\s+.*")) ||
                        nextTrimmed.matches(Regex("^\\d+\\.\\s+.*")) ||
                        nextTrimmed.matches(Regex("^[-*_]{3,}\\s*$"))
                    ) break
                    paragraphLines.add(lines[i])
                    i++
                }
                blocks.add(MarkdownBlock.Paragraph(paragraphLines.joinToString(" ")))
            }
        }
    }
    return blocks
}

/**
 * 时间分隔组件 - 当两条消息间隔超过一小时时显示
 */
@Composable
fun TimeSeparator(
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
    val timeText = dateFormat.format(Date(timestamp))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = timeText,
            fontSize = 12.sp,
            color = DeepSeekColors.Muted,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 聊天头部组件
 */
@Composable
fun ChatHeader(
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepSeekColors.Surface)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onMenuClick != null) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "设置",
                    tint = DeepSeekColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = "ChatBot",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DeepSeekColors.Primary
        )
    }
}

/**
 * 等待回复的打字指示器 - 三个圆点从左到右跑马灯动画
 */
@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    val dotCount = 3
    val animDuration = 800 // 一轮动画时长 ms
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    // 0f → 3f 循环，代表当前高亮到第几个点
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = dotCount.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dot"
    )

    val activeIndex = animProgress.toInt().coerceIn(0, dotCount - 1)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val isActive = index == activeIndex
            val alpha = if (isActive) 1f else 0.3f
            val size = if (isActive) 10.dp else 8.dp
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(DeepSeekColors.Primary.copy(alpha = alpha))
            )
        }
    }
}

/**
 * 加载指示器
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier.size(20.dp),
        color = DeepSeekColors.Primary,
        strokeWidth = 2.dp
    )
}
