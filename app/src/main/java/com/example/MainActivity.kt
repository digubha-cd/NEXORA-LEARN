package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.core.repository.AuthRepositoryImpl
import com.example.ui.navigation.NexoraNavHost
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexoraTheme {
                val authRepository = remember { AuthRepositoryImpl(applicationContext) }

                LaunchedEffect(Unit) {
                    authRepository.checkExistingSession()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NexoraBackground
                ) {
                    NexoraNavHost(authRepository = authRepository)
                }
            }
        }
    }
}

