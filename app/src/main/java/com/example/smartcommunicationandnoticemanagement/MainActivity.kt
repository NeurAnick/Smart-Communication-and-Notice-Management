package com.example.smartcommunicationandnoticemanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.smartcommunicationandnoticemanagement.navigation.AppNavGraph
import com.example.smartcommunicationandnoticemanagement.ui.theme.NoticeSyncTheme
import com.example.smartcommunicationandnoticemanagement.utils.RealtimeNotificationListener
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationListener: RealtimeNotificationListener

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoticeSyncTheme {
                val authState by authViewModel.authState.collectAsStateWithLifecycle()
                val user = authState.currentUser

                // Start listening for notifications once user is loaded
                LaunchedEffect(user) {
                    if (user != null) {
                        notificationListener.startListening(user.semester)
                    }
                }

                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF001F3F)
                ) {
                    AppNavGraph(navController = navController, authViewModel = authViewModel)
                }
            }
        }
    }
}