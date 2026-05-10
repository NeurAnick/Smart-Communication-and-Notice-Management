package com.example.smartcommunicationandnoticemanagement.ui.student.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextDark
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextGray
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import com.example.smartcommunicationandnoticemanagement.viewmodel.NoticeViewModel

@Composable
fun StudentNoticesTab(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    noticeViewModel: NoticeViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val notices by noticeViewModel.notices.collectAsStateWithLifecycle()

    val user = (authState as? AuthState.Authenticated)?.user

    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "CT", "Mid", "Final", "Event", "Emergency", "Vacation")

    val relevantNotices = remember(notices, user, selectedFilter) {
        val filteredForUser = notices.filter { 
            it.targetSemester == "all" || it.targetSemester == user?.semester?.toString() 
        }
        if (selectedFilter == "All") filteredForUser else filteredForUser.filter { it.type == selectedFilter }
    }

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
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "All Notices",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
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
        }

        if (relevantNotices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No notices found", color = TextGray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                items(relevantNotices) { notice ->
                    NoticeCard(
                        notice = notice,
                        isUnread = user != null && !notice.seenBy.containsKey(user.uid),
                        onClick = { navController.navigate(NavRoute.NoticeDetail.withId(notice.noticeId)) }
                    )
                }
            }
        }
    }
}
