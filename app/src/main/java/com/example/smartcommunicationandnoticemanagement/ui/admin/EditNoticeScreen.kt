package com.example.smartcommunicationandnoticemanagement.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
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
import com.example.smartcommunicationandnoticemanagement.data.model.Notice
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextDark
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import com.example.smartcommunicationandnoticemanagement.viewmodel.NoticeViewModel
import com.example.smartcommunicationandnoticemanagement.viewmodel.EditState
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditNoticeScreen(
    navController: NavController,
    noticeId: String,
    authViewModel: AuthViewModel = hiltViewModel(),
    noticeViewModel: NoticeViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val user = (authState as? AuthState.Authenticated)?.user
    
    val notices by noticeViewModel.notices.collectAsStateWithLifecycle()
    val notice = remember(notices) { notices.find { it.noticeId == noticeId } }

    if (notice == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var title by remember(notice) { mutableStateOf(notice.title) }
    var body by remember(notice) { mutableStateOf(notice.body) }
    var selectedType by remember(notice) { mutableStateOf(notice.type) }
    val noticeTypes = listOf("CT", "Mid", "Final", "Event", "Emergency", "Vacation")

    var expandedSemester by remember { mutableStateOf(false) }
    var selectedSemester by remember(notice) { mutableStateOf(notice.targetSemester) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImageUrl by remember(notice) { mutableStateOf(notice.imageUrl) }
    
    val editState by noticeViewModel.editState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(editState) {
        if (editState is EditState.Success) {
            snackbarHostState.showSnackbar("Notice updated!")
            navController.popBackStack()
            noticeViewModel.resetEditState()
        } else if (editState is EditState.Error) {
            snackbarHostState.showSnackbar("Error: ${(editState as EditState.Error).message}")
            noticeViewModel.resetEditState()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Notice", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notice Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Notice Body") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Notice Type", fontWeight = FontWeight.Medium, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                noticeTypes.forEach { type ->
                    val isSelected = selectedType == type
                    Surface(
                        modifier = Modifier.clickable { selectedType = type },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = type,
                            color = if (isSelected) Color.White else TextDark,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Target Semester", fontWeight = FontWeight.Medium, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expandedSemester,
                onExpandedChange = { expandedSemester = !expandedSemester }
            ) {
                OutlinedTextField(
                    value = if (selectedSemester == "all") "All Semesters" else "Semester $selectedSemester",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSemester) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedSemester,
                    onDismissRequest = { expandedSemester = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Semesters") },
                        onClick = { selectedSemester = "all"; expandedSemester = false }
                    )
                    (1..8).forEach { sem ->
                        DropdownMenuItem(
                            text = { Text("Semester $sem") },
                            onClick = { selectedSemester = sem.toString(); expandedSemester = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Change Image")
            }

            if (imageUri != null || currentImageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = imageUri ?: currentImageUrl,
                    contentDescription = "Preview",
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (title.isBlank() || body.isBlank()) {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Title and body are required") }
                        return@Button
                    }
                    coroutineScope.launch {
                        var uploadedImageUrl = currentImageUrl
                        if (imageUri != null) {
                            try {
                                val ref = FirebaseStorage.getInstance().reference.child("notices/${System.currentTimeMillis()}")
                                ref.putFile(imageUri!!).await()
                                uploadedImageUrl = ref.downloadUrl.await().toString()
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Image upload failed")
                            }
                        }

                        val updatedNotice = notice.copy(
                            title = title.trim(),
                            body = body.trim(),
                            type = selectedType,
                            targetSemester = selectedSemester,
                            imageUrl = uploadedImageUrl
                        )

                        noticeViewModel.updateNotice(updatedNotice)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = editState !is EditState.Loading
            ) {
                if (editState is EditState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    noticeViewModel.deleteNotice(noticeId)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
            ) {
                Text("Delete Notice", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
