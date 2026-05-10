package com.example.smartcommunicationandnoticemanagement.ui.admin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartcommunicationandnoticemanagement.data.model.Routine
import com.example.smartcommunicationandnoticemanagement.ui.theme.CardBorder
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextDark
import com.example.smartcommunicationandnoticemanagement.ui.theme.TextGray
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel
import com.example.smartcommunicationandnoticemanagement.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRoutineTab(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    routineViewModel: RoutineViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val adminUser = (authState as? AuthState.Authenticated)?.user

    val routines by routineViewModel.routines.collectAsStateWithLifecycle()
    val isLoading by routineViewModel.isLoading.collectAsStateWithLifecycle()

    val days = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu")
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    var expandedSemester by remember { mutableStateOf(false) }
    var selectedSemester by remember { mutableStateOf(adminUser?.semester ?: 1) }

    var showDialog by remember { mutableStateOf(false) }
    var editingRoutine by remember { mutableStateOf<Routine?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Routine?>(null) }

    LaunchedEffect(selectedSemester) {
        routineViewModel.loadRoutines(selectedSemester)
    }

    val selectedDayStr = when(selectedTabIndex) {
        0 -> "Saturday"
        1 -> "Sunday"
        2 -> "Monday"
        3 -> "Tuesday"
        4 -> "Wednesday"
        else -> "Thursday"
    }

    val dayRoutines = routines.filter { it.day == selectedDayStr }.sortedBy { it.startTime }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRoutine = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Class")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Routine Management",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    if (adminUser?.adminType == "teacher") {
                        ExposedDropdownMenuBox(
                            expanded = expandedSemester,
                            onExpandedChange = { expandedSemester = !expandedSemester },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = "Semester $selectedSemester",
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
                                (1..8).forEach { sem ->
                                    DropdownMenuItem(
                                        text = { Text("Semester $sem") },
                                        onClick = { selectedSemester = sem; expandedSemester = false }
                                    )
                                }
                            }
                        }
                    }

                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 16.dp,
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    ) {
                        days.forEachIndexed { index, day ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { 
                                    Text(
                                        text = day, 
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else TextGray
                                    ) 
                                }
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (dayRoutines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextGray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No classes scheduled", color = TextGray, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dayRoutines) { routine ->
                        AdminRoutineCard(
                            routine = routine,
                            onEdit = { 
                                editingRoutine = routine
                                showDialog = true
                            },
                            onDelete = { showDeleteConfirm = routine }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        RoutineDialog(
            routine = editingRoutine,
            semester = selectedSemester,
            defaultDay = selectedDayStr,
            onDismiss = { showDialog = false },
            onSave = { newRoutine ->
                if (editingRoutine == null) {
                    routineViewModel.addRoutine(newRoutine)
                } else {
                    routineViewModel.updateRoutine(newRoutine)
                }
                showDialog = false
            }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Class") },
            text = { Text("Are you sure you want to delete ${showDeleteConfirm!!.subject}?") },
            confirmButton = {
                TextButton(onClick = {
                    routineViewModel.deleteRoutine(showDeleteConfirm!!.routineId)
                    showDeleteConfirm = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }
}

@Composable
fun AdminRoutineCard(routine: Routine, onEdit: () -> Unit, onDelete: () -> Unit) {
    var expandedMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.subject,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = routine.teacher, fontSize = 14.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = routine.room,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${routine.startTime} - ${routine.endTime}",
                            fontSize = 12.sp,
                            color = TextDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextGray)
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                expandedMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                expandedMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDialog(
    routine: Routine?,
    semester: Int,
    defaultDay: String,
    onDismiss: () -> Unit,
    onSave: (Routine) -> Unit
) {
    var subject by remember { mutableStateOf(routine?.subject ?: "") }
    var teacher by remember { mutableStateOf(routine?.teacher ?: "") }
    var room by remember { mutableStateOf(routine?.room ?: "") }
    var startTime by remember { mutableStateOf(routine?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(routine?.endTime ?: "10:30") }
    
    var selectedDay by remember { mutableStateOf(routine?.day ?: defaultDay) }
    var expandedDay by remember { mutableStateOf(false) }
    val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (routine == null) "Add Class" else "Edit Class") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedDay,
                    onExpandedChange = { expandedDay = !expandedDay }
                ) {
                    OutlinedTextField(
                        value = selectedDay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDay) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDay,
                        onDismissRequest = { expandedDay = false }
                    ) {
                        days.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day) },
                                onClick = { selectedDay = day; expandedDay = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("Teacher") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank() && teacher.isNotBlank() && room.isNotBlank()) {
                        val newRoutine = routine?.copy(
                            day = selectedDay,
                            subject = subject.trim(),
                            teacher = teacher.trim(),
                            room = room.trim(),
                            startTime = startTime.trim(),
                            endTime = endTime.trim()
                        ) ?: Routine(
                            semester = semester,
                            day = selectedDay,
                            subject = subject.trim(),
                            teacher = teacher.trim(),
                            room = room.trim(),
                            startTime = startTime.trim(),
                            endTime = endTime.trim()
                        )
                        onSave(newRoutine)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextGray) }
        }
    )
}
