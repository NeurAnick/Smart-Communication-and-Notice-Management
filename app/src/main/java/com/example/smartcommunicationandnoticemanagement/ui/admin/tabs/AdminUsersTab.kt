package com.example.smartcommunicationandnoticemanagement.ui.admin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.smartcommunicationandnoticemanagement.data.model.User
import com.example.smartcommunicationandnoticemanagement.data.repository.UserRepository
import com.example.smartcommunicationandnoticemanagement.ui.theme.CardBorder
import com.example.smartcommunicationandnoticemanagement.ui.theme.SkyBlueLight
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextDark
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextGray
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    val isLoading = MutableStateFlow(true)

    fun loadUsers(adminType: String, semester: Int) {
        viewModelScope.launch {
            isLoading.value = true
            val flow = if (adminType == "cr") userRepository.getUsersBySemester(semester) else userRepository.getAllUsers()
            flow.collect {
                _users.value = it
                isLoading.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersTab(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    usersViewModel: UsersViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val adminUser = (authState as? AuthState.Authenticated)?.user

    val users by usersViewModel.users.collectAsStateWithLifecycle()
    val isLoading by usersViewModel.isLoading.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(adminUser) {
        if (adminUser != null) {
            usersViewModel.loadUsers(adminUser.adminType, adminUser.semester)
        }
    }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter { 
            it.name.contains(searchQuery, true) || it.studentId.contains(searchQuery, true) 
        }
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Users Management",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or ID") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (filteredUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No users found", color = TextGray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredUsers) { user ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedUser = user
                                showBottomSheet = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = user.name.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.name,
                                    color = TextDark,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${user.studentId} · ${user.department}",
                                    color = TextGray,
                                    fontSize = 12.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SkyBlueLight
                            ) {
                                Text(
                                    text = "Sem ${user.semester}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet && selectedUser != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = selectedUser!!.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = selectedUser!!.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedUser!!.email,
                    fontSize = 14.sp,
                    color = TextGray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val joinedDate = sdf.format(Date(selectedUser!!.createdAt))

                    UserProfileRow("Student ID", selectedUser!!.studentId)
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha=0.5f), modifier = Modifier.padding(vertical = 12.dp))
                    UserProfileRow("Department", selectedUser!!.department)
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha=0.5f), modifier = Modifier.padding(vertical = 12.dp))
                    UserProfileRow("Semester", selectedUser!!.semester.toString())
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha=0.5f), modifier = Modifier.padding(vertical = 12.dp))
                    UserProfileRow("Joined Date", joinedDate)
                    
                    if (selectedUser!!.phone.isNotBlank()) {
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha=0.5f), modifier = Modifier.padding(vertical = 12.dp))
                        UserProfileRow("Phone", selectedUser!!.phone)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun UserProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextGray, fontSize = 14.sp)
        Text(text = value, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
