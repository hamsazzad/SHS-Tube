package com.shsshobuj.shstube.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.shsshobuj.shstube.ui.theme.SHSTubeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Handle shared/intent URLs
        handleIntent(intent)

        setContent {
            SHSTubeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SHSTubeNavHost(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!sharedUrl.isNullOrBlank()) {
                    viewModel.handleSharedUrl(sharedUrl)
                }
            }
            Intent.ACTION_VIEW -> {
                val url = intent.dataString
                if (!url.isNullOrBlank()) {
                    viewModel.handleSharedUrl(url)
                }
            }
        }
    }
}
