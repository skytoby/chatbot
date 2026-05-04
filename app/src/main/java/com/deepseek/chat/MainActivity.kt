package com.deepseek.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.deepseek.chat.data.local.ThemePrefs
import com.deepseek.chat.ui.screen.ChatScreen
import com.deepseek.chat.ui.theme.DeepSeekColors
import com.deepseek.chat.ui.theme.DeepSeekTheme
import com.deepseek.chat.ui.theme.ThemeStore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themePrefs = ThemePrefs(this)

        setContent {
            var themeIndex by remember { mutableIntStateOf(themePrefs.getThemeIndexSync()) }
            val currentTheme = ThemeStore.themes.getOrElse(themeIndex) { ThemeStore.themes[0] }
            val scope = rememberCoroutineScope()

            DeepSeekTheme(appColors = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepSeekColors.Background
                ) {
                    ChatScreen(
                        currentThemeIndex = themeIndex,
                        onThemeSelected = { index ->
                            themeIndex = index
                            scope.launch { themePrefs.setThemeIndex(index) }
                        }
                    )
                }
            }
        }
    }
}
