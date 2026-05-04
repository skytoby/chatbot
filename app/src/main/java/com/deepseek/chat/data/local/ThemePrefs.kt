package com.deepseek.chat.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "theme_prefs")

/**
 * 保存主题选择
 */
class ThemePrefs(private val context: Context) {

    private val appContext = context.applicationContext

    /**
     * 同步读取主题索引（仅用于 Activity 初始化，避免主题闪烁）
     */
    fun getThemeIndexSync(): Int = runBlocking {
        appContext.dataStore.data
            .map { it[KEY_THEME_INDEX] ?: 0 }
            .first()
    }

    suspend fun getThemeIndex(): Int {
        return appContext.dataStore.data
            .map { it[KEY_THEME_INDEX] ?: 0 }
            .first()
    }

    suspend fun setThemeIndex(value: Int) {
        appContext.dataStore.edit { it[KEY_THEME_INDEX] = value }
    }

    companion object {
        private val KEY_THEME_INDEX = intPreferencesKey("theme_index")
    }
}
