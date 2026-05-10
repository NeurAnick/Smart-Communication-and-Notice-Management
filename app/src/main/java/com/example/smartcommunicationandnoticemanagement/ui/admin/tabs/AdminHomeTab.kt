package com.example.smartcommunicationandnoticemanagement.ui.admin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import com.example.smartcommunicationandnoticemanagement.ui.theme.CardBorder
import com.example.smartcommunicationandnoticemanagement.ui.theme.SkyBlueLight
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextDark
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextGray
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import com.example.smartcommunicationandnoticemanagement.viewmodel.NoticeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AdminHomeTab(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    noticeViewModel: NoticeViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val user = authState.currentUser

    val notices by noticeViewModel.notices.collectAsStateWithLifecycle()
    val isRefreshing by noticeViewModel.isRefreshing.collectAsStateWithLifecycle()
    
    val todayNotices = remember(notices) {
        val today = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        notices.count { it.postedAt > today }
    }

    val avgSeenRate = remember(notices) {
        if (notices.isEmpty()) 0
        else {
            val totalSeen = notices.sumOf { it.seenBy.size }
            totalSeen / notices.size
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { noticeViewModel.refresh() }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Welcome, ${user?.name ?: "Admin"}",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val roleText = if (user?.adminType == "cr") "CR — Semester ${user.semester}" else "Teacher"
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = roleText,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminStatCard(title = "Total Notices", value = notices.size.toString(), modifier = Modifier.weight(1f))
                    AdminStatCard(title = "Posted Today", value = todayNotices.toString(), modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminStatCard(title = "Avg Seen Rate", value = "$avgSeenRate views", modifier = Modifier.weight(1f))
                    AdminStatCard(title = "System Health", value = "Optimal", modifier = Modifier.weight(1f))
                }
            }

            item {
                Text(
                    text = "Recent Notices",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(16.dp, 24.dp, 16.dp, 8.dp)
                )
            }

            if (notices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recent notices", color = TextGray)
                    }
                }
            } else {
                items(notices.take(5)) { notice ->
                    var expanded by remember { mutableStateOf(false) }
                    var showDeleteConfirm by remember { mutableStateOf(false) }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { navController.navigate(NavRoute.SeenBy.withId(notice.noticeId)) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SkyBlueLight
                                ) {
                                    Text(
                                        text = notice.type.ifEmpty { "General" },
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                
                                if (user?.role == "admin") {
                                    Box {
                                        IconButton(
                                            onClick = { expanded = true },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Options",
                                                modifier = Modifier.size(20.dp),
                                                tint = TextGray
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Edit") },
                                                onClick = {
                                                    expanded = false
                                                    navController.navigate(NavRoute.EditNotice.withId(notice.noticeId))
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete", color = Color.Red) },
                                                onClick = {
                                                    expanded = false
                                                    showDeleteConfirm = true
                                                }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "${notice.seenBy.size} seen",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = notice.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextDark
                            )
                        }
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Delete Notice") },
                            text = { Text("Are you sure you want to delete this notice?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    noticeViewModel.deleteNotice(notice.noticeId)
                                    showDeleteConfirm = false
                                }) { Text("Delete", color = Color.Red) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                            }
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AdminStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 12.sp, color = TextGray)
        }
    }
}
