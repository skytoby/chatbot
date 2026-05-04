package com.deepseek.chat.data.api

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSource
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 扣子 API 配置
 */
object CozeConfig {
    const val API_BASE_URL = "https://api.coze.cn"
    const val CHAT_ENDPOINT = "/v3/chat"
    const val CONVERSATION_ENDPOINT = "/v1/conversation/create"

    const val API_TOKEN = "pat_TExMqQfy1sPOz0wHe4RiTBpwyqj2UnOFLlQ9GtzNiiEGvkj3HO13NTPhf12fOAjf"
    const val BOT_ID = "7618507630361198602"
    const val USER_ID = "android_user_001"
}

/**
 * 流式响应结果，包含 conversation_id 以便后续复用
 */
data class StreamResult(
    val conversationId: String? = null
)

/**
 * 扣子 API 服务
 */
class LLMApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 创建会话，返回 conversation_id
     */
    fun createConversation(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val requestBody = JSONObject().apply {
            put("meta_data", JSONObject().apply {
                put("uuid", "android_${System.currentTimeMillis()}")
            })
        }.toString()

        val request = Request.Builder()
            .url("${CozeConfig.API_BASE_URL}${CozeConfig.CONVERSATION_ENDPOINT}")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .header("Authorization", "Bearer ${CozeConfig.API_TOKEN}")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e.message ?: "创建会话失败")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val code = json.optInt("code", -1)
                    if (code == 0) {
                        val data = json.optJSONObject("data")
                        val conversationId = data?.optString("id", "") ?: ""
                        if (conversationId.isNotEmpty()) {
                            onSuccess(conversationId)
                        } else {
                            onError("会话 ID 为空")
                        }
                    } else {
                        val msg = json.optString("msg", "创建会话失败")
                        onError("创建会话失败: $msg")
                    }
                } catch (e: Exception) {
                    onError("解析响应失败: ${e.message}")
                }
            }
        })
    }

    /**
     * 流式对话
     *
     * @param message 用户消息
     * @param conversationId 会话 ID（可选，传入则复用已有会话上下文）
     * @param onChunk 收到增量内容的回调
     * @param onComplete 完成回调，返回 StreamResult 包含 conversation_id
     * @param onError 错误回调
     */
    fun streamChat(
        message: String,
        conversationId: String? = null,
        onChunk: (String) -> Unit,
        onComplete: (StreamResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val requestBody = JSONObject().apply {
            put("bot_id", CozeConfig.BOT_ID)
            put("user_id", CozeConfig.USER_ID)
            put("stream", true)
            put("auto_save_history", true)
            put("additional_messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("content", message)
                    put("content_type", "text")
                    put("role", "user")
                    put("type", "question")
                })
            })
        }.toString()

        // conversation_id 作为 query 参数传递
        val urlBuilder = StringBuilder("${CozeConfig.API_BASE_URL}${CozeConfig.CHAT_ENDPOINT}?stream=true")
        if (!conversationId.isNullOrEmpty()) {
            urlBuilder.append("&conversation_id=$conversationId")
        }

        val request = Request.Builder()
            .url(urlBuilder.toString())
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .header("Authorization", "Bearer ${CozeConfig.API_TOKEN}")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e.message ?: "网络错误")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    onError("API 错误: ${response.code} $errorBody")
                    return
                }

                response.body?.let { responseBody ->
                    processStream(responseBody.source(), onChunk, onComplete, onError)
                } ?: onError("空响应")
            }
        })
    }

    /**
     * 处理 SSE 流
     *
     * 扣子 V3 Chat API SSE 格式:
     * event:conversation.chat.created
     * data:{"id":"...","conversation_id":"...","status":"created",...}
     *
     * event:conversation.message.delta
     * data:{"role":"assistant","type":"answer","content":"你好","content_type":"text"}
     *
     * event:conversation.message.completed
     * data:{"role":"assistant","type":"answer","content":"完整内容",...}
     *
     * event:conversation.chat.completed
     * data:{"id":"...","conversation_id":"...","status":"completed",...}
     *
     * event:done
     * data:"[DONE]"
     */
    private fun processStream(
        source: BufferedSource,
        onChunk: (String) -> Unit,
        onComplete: (StreamResult) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            var currentEvent = ""
            var completed = false
            var conversationId: String? = null

            while (!completed) {
                val line = source.readUtf8Line() ?: break

                if (line.startsWith("event:")) {
                    currentEvent = line.removePrefix("event:").trim()
                    continue
                }

                if (line.startsWith("data:")) {
                    val data = line.removePrefix("data:").trim()

                    if (currentEvent == "done" || data == "\"[DONE]\"") {
                        onComplete(StreamResult(conversationId))
                        completed = true
                        continue
                    }

                    try {
                        val json = JSONObject(data)

                        when (currentEvent) {
                            // 对话创建 - 提取 conversation_id
                            "conversation.chat.created" -> {
                                val convId = json.optString("conversation_id", "")
                                if (convId.isNotEmpty()) {
                                    conversationId = convId
                                }
                            }
                            // 增量消息 - 流式打字机效果
                            "conversation.message.delta" -> {
                                val type = json.optString("type", "")
                                if (type == "answer") {
                                    val content = json.optString("content", "")
                                    if (content.isNotEmpty()) {
                                        onChunk(content)
                                    }
                                }
                            }
                            // 对话完成
                            "conversation.chat.completed" -> {
                                val convId = json.optString("conversation_id", "")
                                if (convId.isNotEmpty()) {
                                    conversationId = convId
                                }
                                onComplete(StreamResult(conversationId))
                                completed = true
                            }
                            // 对话失败
                            "conversation.chat.failed" -> {
                                val lastError = json.optJSONObject("last_error")
                                val errorMsg = lastError?.optString("msg", "对话失败")
                                    ?: "对话失败"
                                onError(errorMsg)
                                completed = true
                            }
                        }
                    } catch (e: Exception) {
                        // 忽略非 JSON 数据行
                    }

                    currentEvent = ""
                }
            }
        } catch (e: Exception) {
            onError(e.message ?: "流读取错误")
        }
    }

    fun cancel() {
        client.dispatcher.cancelAll()
    }
}
