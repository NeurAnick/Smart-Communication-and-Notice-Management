package com.example.smartcommunicationandnoticemanagement.ui.notice

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import coil.compose.AsyncImage
import com.example.smartcommunicationandnoticemanagement.navigation.NavRoute
import com.example.smartcommunicationandnoticemanagement.ui.theme.*
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import com.example.smartcommunicationandnoticemanagement.viewmodel.NoticeViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeDetailScreen(
    navController: NavController,
    noticeId: String,
    authViewModel: AuthViewModel = hiltViewModel(),
    noticeViewModel: NoticeViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val user = (authState as? AuthState.Authenticated)?.user

    val notices by noticeViewModel.notices.collectAsStateWithLifecycle()
    val savedNoticeIds by noticeViewModel.savedNoticeIds.collectAsStateWithLifecycle()
    val notice = notices.find { it.noticeId == noticeId }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(noticeId) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            noticeViewModel.markNoticeSeen(noticeId, currentUserId)
            noticeViewModel.loadSavedNotices(currentUserId)
        }
    }

    if (notice == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val isSaved = savedNoticeIds.contains(noticeId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notice Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (user?.role == "admin" || user?.uid == notice.postedBy) {
                        IconButton(onClick = { navController.navigate(NavRoute.EditNotice.withId(noticeId)) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (user != null) {
                FloatingActionButton(
                    onClick = { noticeViewModel.toggleSave(user.uid, noticeId, !isSaved) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Notice"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            val timeAgo = DateUtils.getRelativeTimeSpanString(
                notice.postedAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SkyBlueLight
                ) {
                    Text(
                        text = notice.type.ifEmpty { "General" },
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = notice.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = notice.postedByName.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = notice.postedByName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(text = "${notice.postedByRole.uppercase()} • $timeAgo", fontSize = 12.sp, color = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = notice.body,
                fontSize = 16.sp,
                color = TextDark,
                lineHeight = 24.sp
            )

            if (notice.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = notice.imageUrl,
                        contentDescription = "Notice Image",
                        modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Notice") },
            text = { Text("Are you sure you want to delete this notice? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    noticeViewModel.deleteNotice(noticeId)
                    showDeleteConfirm = false
                    navController.popBackStack()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }
}
