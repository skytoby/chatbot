package com.deepseek.chat.domain.model

/**
 * 聊天消息数据模型
 */
data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 消息角色
 */
enum class MessageRole {
    USER,
    ASSISTANT
}
