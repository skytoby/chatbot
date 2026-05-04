package com.deepseek.chat.mvi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.chat.domain.model.ChatMessage
import com.deepseek.chat.domain.model.MessageRole
import com.deepseek.chat.data.local.ChatDatabase
import com.deepseek.chat.data.local.ChatMessageEntity
import com.deepseek.chat.data.local.ConversationPrefs
import com.deepseek.chat.data.repository.ChatRepositoryImpl
import com.deepseek.chat.domain.repository.ChatRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * MVI ViewModel - 处理聊天逻辑，支持消息持久化
 */
class ChatViewModel(
    application: Application,
    private val repository: ChatRepository = ChatRepositoryImpl()
) : AndroidViewModel(application) {

    private val dao = ChatDatabase.getInstance(application).chatMessageDao()
    private val conversationPrefs = ConversationPrefs(application)

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _effect = Channel<ChatEffect>(Channel.BUFFERED)
    val effect: Flow<ChatEffect> = _effect.receiveAsFlow()

    private var streamingMessageId: String = ""

    // 持有当前会话 ID，实现上下文关联
    private var conversationId: String? = null

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val savedMessages = dao.getAllMessages().map { it.toDomain() }
            conversationId = conversationPrefs.getConversationId()
            if (savedMessages.isNotEmpty()) {
                _state.update { it.copy(messages = savedMessages) }
            } else {
                _state.update { ChatState.Initial }
            }
            if (_state.value.messages.isNotEmpty()) {
                _effect.send(ChatEffect.ScrollToBottom)
            }
        }
    }

    fun processIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.SendMessage -> sendMessage(intent.content)
            is ChatIntent.DeleteMessage -> deleteMessage(intent.messageId)
            is ChatIntent.ClearMessages -> clearMessages()
            is ChatIntent.RetryLastMessage -> retryLastMessage()
        }
    }

    fun updateInputText(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    private fun sendMessage(content: String) {
        val trimmedContent = content.trim()
        if (trimmedContent.isEmpty() || _state.value.isLoading) return

        viewModelScope.launch {
            // 1. 添加用户消息
            val userMessage = ChatMessage(
                id = "user-${System.currentTimeMillis()}",
                role = MessageRole.USER,
                content = trimmedContent
            )

            // 持久化用户消息
            dao.insertMessage(ChatMessageEntity.fromDomain(userMessage))

            _state.update { state ->
                state.copy(
                    messages = state.messages + userMessage,
                    inputText = "",
                    isLoading = true,
                    streamingContent = "",
                    error = null
                )
            }

            _effect.send(ChatEffect.ScrollToBottom)

            // 2. 创建 AI 消息占位符
            streamingMessageId = "assistant-${System.currentTimeMillis()}"
            val assistantMessage = ChatMessage(
                id = streamingMessageId,
                role = MessageRole.ASSISTANT,
                content = "",
                isStreaming = true
            )

            _state.update { state ->
                state.copy(messages = state.messages + assistantMessage)
            }

            // 3. 调用扣子 API，携带 conversationId 实现上下文
            repository.sendMessage(
                messages = _state.value.messages,
                newMessage = trimmedContent,
                conversationId = conversationId,
                onChunk = { chunk ->
                    _state.update { state ->
                        state.copy(streamingContent = state.streamingContent + chunk)
                    }
                },
                onComplete = { returnedConversationId ->
                    // 保存 conversation_id 用于后续对话上下文
                    if (!returnedConversationId.isNullOrEmpty()) {
                        conversationId = returnedConversationId
                        viewModelScope.launch {
                            conversationPrefs.setConversationId(returnedConversationId)
                        }
                    }
                    viewModelScope.launch {
                        val finalContent = _state.value.streamingContent.ifEmpty { "回答已完成" }
                        val finalMessage = ChatMessage(
                            id = streamingMessageId,
                            role = MessageRole.ASSISTANT,
                            content = finalContent,
                            isStreaming = false
                        )

                        // 持久化 AI 回复
                        dao.insertMessage(ChatMessageEntity.fromDomain(finalMessage))

                        _state.update { state ->
                            state.copy(
                                messages = state.messages.map { msg ->
                                    if (msg.id == streamingMessageId) finalMessage else msg
                                },
                                isLoading = false,
                                streamingContent = ""
                            )
                        }
                        _effect.send(ChatEffect.ScrollToBottom)
                    }
                },
                onError = { error ->
                    viewModelScope.launch {
                        val errorContent = "抱歉，发生了错误: $error"
                        val errorMessage = ChatMessage(
                            id = streamingMessageId,
                            role = MessageRole.ASSISTANT,
                            content = errorContent,
                            isStreaming = false
                        )

                        // 持久化错误消息
                        dao.insertMessage(ChatMessageEntity.fromDomain(errorMessage))

                        _state.update { state ->
                            state.copy(
                                messages = state.messages.map { msg ->
                                    if (msg.id == streamingMessageId) errorMessage else msg
                                },
                                isLoading = false,
                                streamingContent = "",
                                error = error
                            )
                        }
                        _effect.send(ChatEffect.ShowError(error))
                    }
                }
            )
        }
    }

    private fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            dao.deleteMessageById(messageId)
            _state.update { state ->
                state.copy(messages = state.messages.filter { it.id != messageId })
            }
        }
    }

    private fun clearMessages() {
        viewModelScope.launch {
            // 清空数据库和会话 ID
            dao.deleteAllMessages()
            conversationPrefs.clear()
            conversationId = null
            _state.update { ChatState.Initial }
        }
    }


    private fun retryLastMessage() {
        val messages = _state.value.messages
        val lastUserMessage = messages.filter { it.role == MessageRole.USER }.lastOrNull()
        if (lastUserMessage != null && !_state.value.isLoading) {
            _state.update { state ->
                state.copy(
                    messages = state.messages.filter { it.role != MessageRole.ASSISTANT || it.id == "welcome" }
                )
            }
            sendMessage(lastUserMessage.content)
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.cancelRequest()
    }
}
