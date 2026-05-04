package com.deepseek.chat.data.repository

import com.deepseek.chat.data.api.LLMApiService
import com.deepseek.chat.domain.model.ChatMessage
import com.deepseek.chat.domain.repository.ChatRepository

/**
 * 聊天仓库实现
 */
class ChatRepositoryImpl(
    private val apiService: LLMApiService = LLMApiService()
) : ChatRepository {

    override fun sendMessage(
        messages: List<ChatMessage>,
        newMessage: String,
        conversationId: String?,
        onChunk: (String) -> Unit,
        onComplete: (conversationId: String?) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.streamChat(
            message = newMessage,
            conversationId = conversationId,
            onChunk = onChunk,
            onComplete = { result ->
                onComplete(result.conversationId)
            },
            onError = onError
        )
    }

    override fun cancelRequest() {
        apiService.cancel()
    }
}
