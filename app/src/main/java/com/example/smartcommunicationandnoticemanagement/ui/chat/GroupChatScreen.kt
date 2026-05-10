package com.example.smartcommunicationandnoticemanagement.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
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
import com.example.smartcommunicationandnoticemanagement.data.model.Message
import com.example.smartcommunicationandnoticemanagement.ui.theme.CardBorder
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextDark
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextGray
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import com.example.smartcommunicationandnoticemanagement.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    semester: Int,
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val user = authState.currentUser

    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isSending by chatViewModel.isSending.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        authViewModel.refreshCurrentUser()
    }

    LaunchedEffect(semester) {
        chatViewModel.loadMessages(semester)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Semester $semester Chat", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message…", color = TextGray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            disabledContainerColor = MaterialTheme.colorScheme.background,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 4,
                        enabled = user != null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank() && user != null) {
                                chatViewModel.sendMessage(semester, user.uid, user.name, textInput.trim())
                                textInput = ""
                            }
                        },
                        enabled = !isSending && textInput.isNotBlank() && user != null,
                        modifier = Modifier
                            .background(
                                if (!isSending && textInput.isNotBlank() && user != null) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    Color.LightGray, 
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No messages yet. Say hello!", color = TextGray)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    val isSentByMe = user?.uid == message.senderId
                    MessageBubble(message = message, isSentByMe = isSentByMe)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isSentByMe: Boolean) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isSentByMe) Alignment.End else Alignment.Start
    ) {
        if (!isSentByMe) {
            Text(
                text = message.senderName,
                fontSize = 11.sp,
                color = TextGray,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }
        
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isSentByMe) 16.dp else 4.dp,
                bottomEnd = if (isSentByMe) 4.dp else 16.dp
            ),
            color = if (isSentByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            border = if (isSentByMe) null else androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
        ) {
            Text(
                text = message.text,
                color = if (isSentByMe) Color.White else TextDark,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
        
        Text(
            text = formattedTime,
            fontSize = 10.sp,
            color = TextGray,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}
