package com.deepseek.chat.domain.usecase

import com.deepseek.chat.domain.model.ChatMessage
import com.deepseek.chat.domain.repository.ChatRepository

/**
 * 发送聊天消息用例
 */
class SendMessageUseCase(
    private val repository: ChatRepository
) {
    operator fun invoke(
        messages: List<ChatMessage>,
        newMessage: String,
        conversationId: String? = null,
        onChunk: (String) -> Unit,
        onComplete: (conversationId: String?) -> Unit,
        onError: (String) -> Unit
    ) {
        repository.sendMessage(messages, newMessage, conversationId, onChunk, onComplete, onError)
    }
}

/**
 * 取消请求用例
 */
class CancelRequestUseCase(
    private val repository: ChatRepository
) {
    operator fun invoke() {
        repository.cancelRequest()
    }
}
