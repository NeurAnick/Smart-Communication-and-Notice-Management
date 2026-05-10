package com.example.smartcommunicationandnoticemanagement.ui.student.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.smartcommunicationandnoticemanagement.navigation.NavRoute
import com.example.smartcommunicationandnoticemanagement.ui.theme.SkyBlueLight
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextDark
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextGray
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel

@Composable
fun StudentChatTab(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val user = authState.currentUser

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Text(
                text = "Group Chats",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(12) { index ->
                val semester = index + 1
                // Admins (Teachers) can see all, Students only their own
                val canAccess = user?.role == "admin" || user?.semester == semester
                val isUserSemester = user?.semester == semester

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = canAccess) {
                            navController.navigate(NavRoute.GroupChat.withSemester(semester))
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isUserSemester -> SkyBlueLight
                        !canAccess -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.surface
                    },
                    border = if (isUserSemester) 
                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) 
                    else 
                        borderStroke(),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (canAccess) 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else 
                                Color.LightGray.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (canAccess) Icons.Default.Forum else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (canAccess) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Semester $semester Group",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAccess) TextDark else Color.Gray
                                )
                                if (isUserSemester) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "Your Group",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (canAccess) "${semester}th Semester Students" else "Access Restricted",
                                fontSize = 13.sp,
                                color = TextGray
                            )
                        }

                        if (canAccess) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Enter",
                                tint = TextGray
                            )
                        }
                    }
                }
            }
        }
    }
}
