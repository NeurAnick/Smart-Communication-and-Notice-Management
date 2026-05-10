package com.example.smartcommunicationandnoticemanagement.ui.admin.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.smartcommunicationandnoticemanagement.data.model.User
import com.example.smartcommunicationandnoticemanagement.navigation.NavRoute
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel

@Composable
fun AdminProfileTab(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val user = authState.currentUser

    // Refresh user data when screen opens
    LaunchedEffect(Unit) {
        authViewModel.refreshCurrentUser()
    }

    // NEVER do early return — always show something
    when {
        user == null -> {
            // Show loading spinner while user data loads
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF0078D4))
            }
        }
        else -> {
            // Show actual profile content
            AdminProfileContent(
                user = user,
                authViewModel = authViewModel,
                navController = navController,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun AdminProfileContent(
    user: User,
    authViewModel: AuthViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Avatar circle with initials
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Color(0xFF0078D4)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "A",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = user.name.ifEmpty { "No Name" },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A2E)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Role badge
        val badgeText = when {
            user.adminType == "teacher" -> "Teacher"
            user.adminType == "cr" && user.semester > 0 -> "CR — Semester ${user.semester}"
            user.adminType == "cr" -> "CR"
            else -> "Admin"
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFE1F0FF)
        ) {
            Text(
                text = badgeText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = Color(0xFF005FA3),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(label = "Email", value = user.email.ifEmpty { "Not set" })
                Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                ProfileInfoRow(label = "Department", value = user.department.ifEmpty { "Not set" })
                if (user.adminType == "teacher") {
                    Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                    ProfileInfoRow(label = "Employee ID", value = user.studentId.ifEmpty { "Not set" })
                }
                if (user.phone.isNotEmpty()) {
                    Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                    ProfileInfoRow(label = "Phone", value = user.phone)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Edit Profile button
        Button(
            onClick = { navController.navigate(NavRoute.EditProfile.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D4))
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Logout button
        OutlinedButton(
            onClick = {
                authViewModel.logout()
                navController.navigate(NavRoute.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFD32F2F))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1A1A2E),
            fontWeight = FontWeight.Medium
        )
    }
}