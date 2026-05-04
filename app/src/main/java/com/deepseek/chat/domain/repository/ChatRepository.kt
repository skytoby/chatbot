package com.deepseek.chat.domain.repository

import com.deepseek.chat.domain.model.ChatMessage

/**
 * 聊天仓库接口
 */
interface ChatRepository {
    /**
     * 发送消息并接收流式响应
     *
     * @param conversationId 会话 ID（可选，传入则携带上下文）
     */
    fun sendMessage(
        messages: List<ChatMessage>,
        newMessage: String,
        conversationId: String? = null,
        onChunk: (String) -> Unit,
        onComplete: (conversationId: String?) -> Unit,
        onError: (String) -> Unit
    )

    fun cancelRequest()
}
