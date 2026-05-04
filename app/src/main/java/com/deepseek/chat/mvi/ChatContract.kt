package com.deepseek.chat.mvi

import com.deepseek.chat.domain.model.ChatMessage
import com.deepseek.chat.domain.model.MessageRole

/**
 * MVI Intent - 用户意图/动作
 */
sealed class ChatIntent {
    data class SendMessage(val content: String) : ChatIntent()
    data class DeleteMessage(val messageId: String) : ChatIntent()
    data object ClearMessages : ChatIntent()
    data object RetryLastMessage : ChatIntent()
}

/**
 * MVI State - 视图状态
 */
data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val streamingContent: String = "",
    val error: String? = null
) {
    companion object {
        val Initial = ChatState(
            messages = listOf(
                ChatMessage(
                    id = "welcome",
                    role = MessageRole.ASSISTANT,
                    content = "你好！我是你的AI助手。有什么我可以帮助你的吗？"
                )
            )
        )
    }
}

/**
 * MVI Effect - 副作用（一次性事件）
 */
sealed class ChatEffect {
    data object ScrollToBottom : ChatEffect()
    data class ShowError(val message: String) : ChatEffect()
}
