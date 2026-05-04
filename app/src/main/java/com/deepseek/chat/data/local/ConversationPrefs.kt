package com.deepseek.chat.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "conversation_prefs")

/**
 * 保存 conversationId，用于恢复对话上下文
 */
class ConversationPrefs(private val context: Context) {

    private val appContext = context.applicationContext

    suspend fun getConversationId(): String? {
        return appContext.dataStore.data
            .map { it[KEY_CONVERSATION_ID] }
            .first()
    }

    suspend fun setConversationId(value: String?) {
        appContext.dataStore.edit { prefs ->
            if (value != null) {
                prefs[KEY_CONVERSATION_ID] = value
            } else {
                prefs.remove(KEY_CONVERSATION_ID)
            }
        }
    }

    suspend fun clear() {
        appContext.dataStore.edit { it.clear() }
    }

    companion object {
        private val KEY_CONVERSATION_ID = stringPreferencesKey("conversation_id")
    }
}
