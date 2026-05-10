package com.example.smartcommunicationandnoticemanagement.ui.student.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.smartcommunicationandnoticemanagement.navigation.NavRoute
import com.example.smartcommunicationandnoticemanagement.ui.theme.SkyBlueLight
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextDark
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextGray
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import com.example.smartcommunicationandnoticemanagement.viewmodel.UserViewModel

@Composable
fun StudentProfileTab(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val authUser = authState.currentUser
    
    // Refresh user data on launch to ensure profile completeness
    LaunchedEffect(Unit) {
        authViewModel.refreshCurrentUser()
    }

    LaunchedEffect(authUser?.uid) {
        authUser?.uid?.let { uid ->
            userViewModel.loadCurrentUser(uid)
        }
    }

    val user by userViewModel.currentUser.collectAsStateWithLifecycle()
    val currentUser = user // Capture delegated property for easier use

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Text(
                text = "Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar & Info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = currentUser?.name?.take(1)?.uppercase() ?: "U",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentUser?.name ?: "Student",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SkyBlueLight
            ) {
                Text(
                    text = "Semester ${currentUser?.semester ?: 0}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Details Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileDetailRow(label = "Student ID", value = currentUser?.studentId ?: "")
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
                ProfileDetailRow(label = "Department", value = currentUser?.department ?: "")
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
                ProfileDetailRow(label = "Email", value = currentUser?.email ?: "")
                
                if (currentUser?.phone?.isNotBlank() == true) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
                    ProfileDetailRow(label = "Phone", value = currentUser.phone)
                }
                
                if (currentUser?.bio?.isNotBlank() == true) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
                    ProfileDetailRow(label = "Bio", value = currentUser.bio)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Button(
                onClick = { navController.navigate(NavRoute.EditProfile.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    authViewModel.logout()
                    navController.navigate(NavRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextGray, fontSize = 14.sp)
        Text(text = value, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}