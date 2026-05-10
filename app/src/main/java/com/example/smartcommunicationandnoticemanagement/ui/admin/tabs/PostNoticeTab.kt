package com.example.smartcommunicationandnoticemanagement.ui.admin.tabs

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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.smartcommunicationandnoticemanagement.data.model.Notice
import com.example.smartcommunicationandnoticemanagement.ui.theme.SkyBlue
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextDark
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import com.example.smartcommunicationandnoticemanagement.viewmodel.NoticeViewModel
import com.example.smartcommunicationandnoticemanagement.viewmodel.PostState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostNoticeTab(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    noticeViewModel: NoticeViewModel = hiltViewModel(),
    onPostSuccess: () -> Unit,
    onPostError: (String) -> Unit
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val user = authState.currentUser

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("CT") }
    val noticeTypes = listOf("CT", "Mid", "Final", "Event", "Emergency", "Vacation")

    var expandedSemester by remember { mutableStateOf(false) }
    var selectedSemester by remember(user) { mutableStateOf(if (user?.adminType == "cr") user.semester.toString() else "all") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadedImageUrl by remember { mutableStateOf("") }

    val postState by noticeViewModel.postState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            coroutineScope.launch {
                isUploading = true
                uploadedImageUrl = noticeViewModel.uploadImage(it)
                isUploading = false
            }
        }
    }

    LaunchedEffect(postState) {
        val currentState = postState
        when (currentState) {
            is PostState.Success -> {
                title = ""
                body = ""
                imageUri = null
                uploadedImageUrl = ""
                selectedType = "CT"
                onPostSuccess()
                noticeViewModel.resetPostState()
            }
            is PostState.Error -> {
                onPostError(currentState.message)
                noticeViewModel.resetPostState()
            }
            else -> {}
        }
    }

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
                text = "Post New Notice",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(16.dp)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notice Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Notice Body") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 10,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
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

            if (user?.adminType == "cr") {
                OutlinedTextField(
                    value = "Semester ${user.semester}",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = TextDark,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    enabled = false
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = expandedSemester,
                    onExpandedChange = { expandedSemester = !expandedSemester }
                ) {
                    OutlinedTextField(
                        value = if (selectedSemester == "all") "All Semesters" else "Semester $selectedSemester",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSemester) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (imageUri != null) "Image Selected ✓" else "Attach Image (Optional)")
            }

            if (isUploading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = SkyBlue
                )
            }

            if (imageUri != null) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (user == null) {
                        onPostError("Admin profile not loaded yet. Please wait.")
                        return@Button
                    }
                    if (title.isBlank() || body.isBlank()) {
                        onPostError("Please enter both Title and Body.")
                        return@Button
                    }
                    
                    val notice = Notice(
                        noticeId = "",
                        title = title.trim(),
                        body = body.trim(),
                        type = selectedType,
                        targetSemester = selectedSemester,
                        postedBy = user.uid,
                        postedByName = user.name,
                        postedByRole = user.adminType.ifEmpty { "teacher" },
                        imageUrl = uploadedImageUrl,
                        postedAt = System.currentTimeMillis(),
                        isPinned = false,
                        seenBy = emptyMap()
                    )

                    noticeViewModel.postNotice(notice)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                enabled = !isUploading && postState !is PostState.Loading
            ) {
                if (postState is PostState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Post Notice", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
