package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ChatScreen
import com.example.ui.ChatViewModel
import com.example.ui.accentThemes
import com.example.ui.theme.NexusAiTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val selectedAccentColorIndex by viewModel.selectedAccentColorIndex.collectAsStateWithLifecycle()
            val accentColor = accentThemes.getOrNull(selectedAccentColorIndex)?.color
                ?: androidx.compose.ui.graphics.Color(0xFF4F46E5)

            NexusAiTheme(
                themeMode = themeMode,
                accentColor = accentColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }
}
