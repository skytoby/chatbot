package com.deepseek.chat.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.navigationBarsPadding
import android.app.Application
import android.widget.Toast
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepseek.chat.data.local.ThemePrefs
import com.deepseek.chat.mvi.ChatEffect
import com.deepseek.chat.mvi.ChatIntent
import com.deepseek.chat.mvi.ChatState
import com.deepseek.chat.mvi.ChatViewModel
import com.deepseek.chat.ui.components.ChatHeader
import com.deepseek.chat.ui.components.ChatInput
import com.deepseek.chat.ui.components.MarkdownText
import com.deepseek.chat.ui.components.MessageBubble
import com.deepseek.chat.ui.components.SettingsPanel
import com.deepseek.chat.ui.components.TimeSeparator
import com.deepseek.chat.ui.components.TypingIndicator
import com.deepseek.chat.ui.theme.DeepSeekColors
import com.deepseek.chat.ui.theme.ThemeStore
import kotlinx.coroutines.launch

/**
 * 聊天主屏幕（带设置抽屉）
 */
@Composable
fun ChatScreen(
    currentThemeIndex: Int,
    onThemeSelected: (Int) -> Unit
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModel: ChatViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(app) as T
            }
        }
    )
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var longPressedMessage by remember { mutableStateOf<com.deepseek.chat.domain.model.ChatMessage?>(null) }
    var selectTextMessage by remember { mutableStateOf<com.deepseek.chat.domain.model.ChatMessage?>(null) }
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // 处理副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ChatEffect.ScrollToBottom -> {
                    scope.launch {
                        if (state.messages.isNotEmpty()) {
                            listState.animateScrollToItem(state.messages.size)
                        }
                    }
                }
                is ChatEffect.ShowError -> {
                    // 错误已通过消息展示
                }
            }
        }
    }

    // 首次加载时直接定位到底部（无动画）
    var initialScrollDone by remember { mutableStateOf(false) }
    LaunchedEffect(state.messages.size) {
        if (!initialScrollDone && state.messages.isNotEmpty()) {
            listState.scrollToItem(state.messages.size)
            initialScrollDone = true
        }
    }

    // 新消息或流式内容更新时自动滚动到底部
    val prevMessageCount = remember { mutableIntStateOf(state.messages.size) }
    LaunchedEffect(state.messages.size, state.streamingContent) {
        if (initialScrollDone) {
            if (state.messages.size > prevMessageCount.intValue || state.streamingContent != null) {
                listState.animateScrollToItem(state.messages.size)
            }
            prevMessageCount.intValue = state.messages.size
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SettingsPanel(
                currentThemeIndex = currentThemeIndex,
                onThemeSelected = { index ->
                    onThemeSelected(index)
                    scope.launch { drawerState.close() }
                }
            )
        },
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepSeekColors.Background)
        ) {
            // 顶部 Header
            ChatHeader(
                onMenuClick = { scope.launch { drawerState.open() } }
            )

            // 消息列表
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    items = state.messages,
                    key = { _, message -> message.id }
                ) { index, message ->
                    // 如果与上一条消息间隔超过一小时，插入时间分隔
                    if (index > 0) {
                        val prevTimestamp = state.messages[index - 1].timestamp
                        val currentTimestamp = message.timestamp
                        if (currentTimestamp - prevTimestamp >= 3600_000L) {
                            TimeSeparator(timestamp = currentTimestamp)
                        }
                    }

                    // 正在流式接收的消息：显示打字动画或实时内容
                    if (message.isStreaming && state.isLoading) {
                        if (state.streamingContent.isEmpty()) {
                            // 等待服务器首个响应：显示三点跑马灯动画
                            MessageBubble(
                                message = message.copy(content = ""),
                                onLongClick = null,
                                typingIndicator = true
                            )
                        } else {
                            // 已收到流式内容：实时显示
                            MessageBubble(
                                message = message.copy(content = state.streamingContent),
                                onLongClick = null
                            )
                        }
                    } else {
                        MessageBubble(
                            message = message,
                            onLongClick = { longPressedMessage = message }
                        )
                    }
                }
                // 底部占位，确保能滚动到最后一条消息的底部
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }

            // 底部输入区域
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = DeepSeekColors.Surface,
                tonalElevation = 0.dp
            ) {
                ChatInput(
                    value = state.inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    onSend = { viewModel.processIntent(ChatIntent.SendMessage(state.inputText)) },
                    isLoading = state.isLoading
                )
            }
        }
    }

    // 长按操作菜单
    if (longPressedMessage != null) {
        AlertDialog(
            onDismissRequest = { longPressedMessage = null },
            title = { Text("操作") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("message", longPressedMessage!!.content))
                            Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                            longPressedMessage = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("复制消息", color = DeepSeekColors.OnSurface)
                    }
                    TextButton(
                        onClick = {
                            selectTextMessage = longPressedMessage
                            longPressedMessage = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("选取文字", color = DeepSeekColors.OnSurface)
                    }
                    TextButton(
                        onClick = {
                            viewModel.processIntent(ChatIntent.DeleteMessage(longPressedMessage!!.id))
                            longPressedMessage = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("删除消息", color = Color.Red)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { longPressedMessage = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 选取文字对话框
    if (selectTextMessage != null) {
        AlertDialog(
            onDismissRequest = { selectTextMessage = null },
            title = { Text("选取文字") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SelectionContainer {
                        MarkdownText(
                            markdown = selectTextMessage!!.content,
                            textColor = DeepSeekColors.OnSurface
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectTextMessage = null }) {
                    Text("完成")
                }
            }
        )
    }
}
