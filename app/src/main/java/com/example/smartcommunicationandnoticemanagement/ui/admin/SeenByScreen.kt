package com.example.smartcommunicationandnoticemanagement.ui.admin

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.smartcommunicationandnoticemanagement.data.model.User
import com.example.smartcommunicationandnoticemanagement.data.repository.NoticeRepository
import com.example.smartcommunicationandnoticemanagement.data.repository.UserRepository
import com.example.smartcommunicationandnoticemanagement.ui.theme.CardBorder
import com.example.smartcommunicationandnoticemanagement.ui.theme.SkyBlueLight
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextDark
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextGray
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeenUser(val user: User, val seenAt: Long)

@HiltViewModel
class SeenByViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _seenUsers = MutableStateFlow<List<SeenUser>?>(null)
    val seenUsers: StateFlow<List<SeenUser>?> = _seenUsers

    fun loadSeenBy(noticeId: String) {
        viewModelScope.launch {
            noticeRepository.getSeenByList(noticeId).collect { seenMap ->
                val list = mutableListOf<SeenUser>()
                for ((uid, timestamp) in seenMap) {
                    val u = userRepository.getUser(uid)
                    if (u != null) {
                        list.add(SeenUser(u, timestamp))
                    }
                }
                _seenUsers.value = list.sortedByDescending { it.seenAt }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeenByScreen(
    noticeId: String,
    navController: NavController,
    viewModel: SeenByViewModel = hiltViewModel()
) {
    val seenUsers by viewModel.seenUsers.collectAsStateWithLifecycle()

    LaunchedEffect(noticeId) {
        viewModel.loadSeenBy(noticeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seen By", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (seenUsers == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (seenUsers!!.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No one has seen this notice yet", color = TextGray, fontSize = 16.sp)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = SkyBlueLight
                    ) {
                        Text(
                            text = "Seen by ${seenUsers!!.size} students",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(seenUsers!!) { seenData ->
                            val timeAgo = DateUtils.getRelativeTimeSpanString(
                                seenData.seenAt,
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS
                            ).toString()

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = seenData.user.name.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = seenData.user.name,
                                            color = TextDark,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Semester ${seenData.user.semester} · ${seenData.user.department}",
                                            color = TextGray,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Text(
                                        text = timeAgo,
                                        color = TextGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
