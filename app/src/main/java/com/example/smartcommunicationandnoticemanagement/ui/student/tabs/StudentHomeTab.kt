package com.example.smartcommunicationandnoticemanagement.ui.student.tabs

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.smartcommunicationandnoticemanagement.data.model.Notice
import com.example.smartcommunicationandnoticemanagement.navigation.NavRoute
import com.example.smartcommunicationandnoticemanagement.ui.theme.*
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import com.example.smartcommunicationandnoticemanagement.viewmodel.NoticeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun StudentHomeTab(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    noticeViewModel: NoticeViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val notices by noticeViewModel.notices.collectAsStateWithLifecycle()
    val isRefreshing by noticeViewModel.isRefreshing.collectAsStateWithLifecycle()

    val user = authState.currentUser

    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "CT", "Mid", "Final", "Event", "Emergency", "Vacation")

    val relevantNotices = remember(notices, user, selectedFilter) {
        val filteredForUser = notices.filter { 
            it.targetSemester == "all" || it.targetSemester == user?.semester?.toString() 
        }
        if (selectedFilter == "All") filteredForUser else filteredForUser.filter { it.type == selectedFilter }
    }

    val unreadCount = remember(relevantNotices, user) {
        if (user == null) 0 else relevantNotices.count { !it.seenBy.containsKey(user.uid) }
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
                // Welcome Header
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Hello, ${user?.name ?: ""}",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Semester ${user?.semester ?: 0} · ${user?.department ?: ""} · Premier University",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item {
                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = borderStroke(),
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = unreadCount.toString(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Unread Notices", fontSize = 12.sp, color = TextGray)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = borderStroke(),
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = relevantNotices.size.toString(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text("Total Notices", fontSize = 12.sp, color = TextGray)
                        }
                    }
                }
            }

            item {
                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterOptions.forEach { option ->
                        val isSelected = selectedFilter == option
                        Surface(
                            modifier = Modifier.clickable { selectedFilter = option },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) null else borderStroke()
                        ) {
                            Text(
                                text = option,
                                color = if (isSelected) Color.White else TextDark,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (relevantNotices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notices found", color = TextGray, fontSize = 16.sp)
                    }
                }
            } else {
                items(relevantNotices) { notice ->
                    NoticeCard(
                        notice = notice,
                        isUnread = user != null && !notice.seenBy.containsKey(user.uid),
                        onClick = { navController.navigate(NavRoute.NoticeDetail.withId(notice.noticeId)) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
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
fun NoticeCard(notice: Notice, isUnread: Boolean, onClick: () -> Unit) {
    val (typeColor, typeBg) = when (notice.type) {
        "CT" -> TypeCT to TypeCTBg
        "Mid" -> TypeMid to TypeMidBg
        "Final" -> TypeFinal to TypeFinalBg
        "Event" -> TypeEvent to TypeEventBg
        "Emergency" -> TypeEmergency to TypeEmergencyBg
        "Vacation" -> TypeVacation to TypeVacationBg
        else -> MaterialTheme.colorScheme.primary to SkyBlueLight
    }

    val timeAgo = DateUtils.getRelativeTimeSpanString(
        notice.postedAt,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = borderStroke(),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeBg
                    ) {
                        Text(
                            text = notice.type.ifEmpty { "General" },
                            color = typeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (notice.isPinned) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = "PINNED",
                                color = Color(0xFFE65100),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = notice.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "${notice.postedByName} • $timeAgo",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
            
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun borderStroke() = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
