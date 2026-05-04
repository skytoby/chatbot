package com.deepseek.chat.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.deepseek.chat.domain.model.ChatMessage
import com.deepseek.chat.domain.model.MessageRole

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): ChatMessage = ChatMessage(
        id = id,
        role = MessageRole.valueOf(role),
        content = content,
        isStreaming = false,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(message: ChatMessage): ChatMessageEntity = ChatMessageEntity(
            id = message.id,
            role = message.role.name,
            content = message.content
        )
    }
}
