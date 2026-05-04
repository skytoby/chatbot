# ChatBot - Android App

类似 DeepSeek 的 AI 问答 Android 应用，使用 Kotlin Compose 和 MVI 架构实现。

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **架构**: MVI (Model-View-Intent)
- **HTTP Client**: OkHttp (支持 SSE 流式输出)
- **最低 SDK**: Android 8.0 (API 26)
- **LLM**: 扣子 (Coze.cn) API - **直接调用，无需后端**

## 项目结构

```
android/
├── app/src/main/java/com/deepseek/chat/
│   ├── MainActivity.kt
│   ├── domain/                    # 领域层
│   │   ├── model/                # 数据模型
│   │   ├── repository/           # 仓库接口
│   │   └── usecase/             # 用例
│   ├── data/                     # 数据层
│   │   ├── api/                  # API 服务 (LLMApiService - 直连扣子)
│   │   └── repository/           # 仓库实现
│   ├── mvi/                      # MVI 架构
│   │   ├── ChatContract.kt      # Intent/State/Effect
│   │   └── ChatViewModel.kt     # ViewModel
│   └── ui/                       # 表现层
│       ├── theme/               # 主题配置
│       ├── screen/              # 页面
│       └── components/          # 组件
```

## 扣子 API 直接接入

### 架构说明

```
┌─────────────────┐      ┌─────────────────┐
│  Android App    │ ───▶ │   扣子 API      │
│  (Kotlin)       │      │   api.coze.cn   │
└─────────────────┘      └─────────────────┘

直接调用，无需后端服务！


┌─────────────────────────────────────────┐
│              MainActivity                │
│          主题初始化 + Compose 入口         │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│             ChatScreen                   │
│   消息列表 + 输入框 + 设置抽屉 + 加载动画  │
└──────┬──────────────────────┬────────────┘
       │ Intent               │ State / Effect
       ▼                      │
┌──────────────────┐          │
│  ChatViewModel   │◀─────────┘
│  MVI 状态管理     │
└──────┬───────────┘
       │
  ┌────┴────────────────┐
  ▼                      ▼
┌──────────────┐  ┌──────────────┐
│ Repository   │  │   Room DB    │
│ (数据仓库)    │  │ (本地持久化)  │
└──────┬───────┘  └──────────────┘
       │
       ▼
┌──────────────┐
│ LLMApiService│
│ OkHttp + SSE │
└──────┬───────┘
       │ HTTPS
       ▼
┌──────────────┐
│   Coze API   │
│  api.coze.cn │
└──────────────┘
```

### 配置步骤

#### 1. 获取扣子 API Token

访问 [扣子开放平台](https://www.coze.cn/open) 获取 API Token。

#### 2. 配置 Android APP

编辑 `data/api/LLMApiService.kt` 中的 `CozeConfig`：

```kotlin
object CozeConfig {
    // 扣子 API 端点
    const val API_BASE_URL = "https://api.coze.cn"

    // 你的扣子 API Token
    const val API_TOKEN = "your_api_token_here"

    // Bot ID (从扣子平台获取)
    const val BOT_ID = "your_bot_id_here"

    // 用户标识
    const val USER_ID = "android_user_001"
}
```

### 扣子 API 请求格式

```bash
curl -X POST 'https://api.coze.cn/v3/chat' \
  -H "Authorization: Bearer $COZE_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bot_id": "7619176807107428352",
    "user_id": "123456789",
    "stream": true,
    "additional_messages": [
      {
        "content": "hello",
        "content_type": "text",
        "role": "user",
        "type": "question"
      }
    ],
    "parameters": {}
  }'
```

## MVI 架构

### Intent (用户意图)

```kotlin
sealed class ChatIntent {
    data class SendMessage(val content: String) : ChatIntent()
    data object ClearMessages : ChatIntent()
    data object RetryLastMessage : ChatIntent()
}
```

### State (视图状态)

```kotlin
data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val streamingContent: String = "",
    val error: String? = null
)
```

### Effect (副作用)

```kotlin
sealed class ChatEffect {
    data object ScrollToBottom : ChatEffect()
    data class ShowError(val message: String) : ChatEffect()
}
```

## 构建和运行

### 1. 构建 Debug APK

```bash
cd android
./gradlew assembleDebug
```

### 2. 安装到设备

```bash
./gradlew installDebug
```

## 功能特性

- DeepSeek 风格深色主题 UI
- 扣子 LLM **直接调用** - 无需后端服务
- SSE 流式对话响应
- MVI 架构状态管理
- 自动滚动到最新消息

## 注意事项

1. **API Token 安全**: 建议生产环境中使用混淆或服务器代理保护 API Token
2. **网络权限**: 已配置 `android.permission.INTERNET`
3. **HTTPS**: 扣子 API 使用 HTTPS，安全可靠
