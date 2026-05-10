package com.example.smartcommunicationandnoticemanagement.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.smartcommunicationandnoticemanagement.navigation.NavRoute
import com.example.smartcommunicationandnoticemanagement.ui.student.tabs.*
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel

@Composable
fun StudentDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Notices", "Chat", "Routine", "Profile")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Notifications,
        Icons.Default.Forum,
        Icons.Default.CalendarToday,
        Icons.Default.Person
    )

    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val user = authState.currentUser

    LaunchedEffect(Unit) {
        authViewModel.refreshCurrentUser()
    }

    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> StudentHomeTab(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                authViewModel = authViewModel
            )
            1 -> StudentNoticesTab(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                authViewModel = authViewModel
            )
            2 -> StudentChatTab(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                authViewModel = authViewModel
            )
            3 -> StudentRoutineTab(
                modifier = Modifier.padding(paddingValues),
                authViewModel = authViewModel
            )
            4 -> StudentProfileTab(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}