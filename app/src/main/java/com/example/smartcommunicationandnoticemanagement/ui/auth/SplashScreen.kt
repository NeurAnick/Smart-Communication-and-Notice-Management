package com.example.smartcommunicationandnoticemanagement.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.smartcommunicationandnoticemanagement.navigation.NavRoute
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        delay(1500L)
        authViewModel.checkCurrentUser()
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                val user = (authState as AuthState.Authenticated).user
                val destination = when (user.role) {
                    "admin" -> NavRoute.AdminDashboard.route
                    else -> NavRoute.StudentDashboard.route
                }
                navController.navigate(destination) {
                    popUpTo(NavRoute.Splash.route) { inclusive = true }
                }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate(NavRoute.Login.route) {
                    popUpTo(NavRoute.Splash.route) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                navController.navigate(NavRoute.Login.route) {
                    popUpTo(NavRoute.Splash.route) { inclusive = true }
                }
            }
            else -> Unit // Loading or Idle state — stay on splash
        }
    }

    // Splash UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0078D4)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF0078D4),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NoticeSync",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Premier University Chittagong",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
