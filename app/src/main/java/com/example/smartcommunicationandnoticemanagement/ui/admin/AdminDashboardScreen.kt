package com.example.smartcommunicationandnoticemanagement.ui.admin

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
import com.example.smartcommunicationandnoticemanagement.ui.admin.tabs.*
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val user = authState.currentUser

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Post", "Users", "Routine", "Profile")
    val icons = listOf(
        Icons.Default.Dashboard,
        Icons.Default.AddCircle,
        Icons.Default.People,
        Icons.Default.EditCalendar,
        Icons.Default.Person
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            0 -> AdminHomeTab(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                authViewModel = authViewModel
            )
            1 -> PostNoticeTab(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                authViewModel = authViewModel,
                onPostSuccess = { 
                    scope.launch {
                        snackbarHostState.showSnackbar("Notice posted successfully!")
                    }
                    selectedTab = 0 
                },
                onPostError = { error ->
                    scope.launch {
                        snackbarHostState.showSnackbar("Error: $error")
                    }
                }
            )
            2 -> AdminUsersTab(
                modifier = Modifier.padding(paddingValues),
                authViewModel = authViewModel
            )
            3 -> AdminRoutineTab(
                modifier = Modifier.padding(paddingValues),
                authViewModel = authViewModel
            )
            4 -> AdminProfileTab(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}