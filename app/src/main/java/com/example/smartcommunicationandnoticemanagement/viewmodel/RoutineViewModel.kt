package com.example.smartcommunicationandnoticemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcommunicationandnoticemanagement.data.model.Routine
import com.example.smartcommunicationandnoticemanagement.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository
) : ViewModel() {
    val routines = MutableStateFlow<List<Routine>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    fun loadRoutines(semester: Int) {
        viewModelScope.launch {
            isLoading.value = true
            routineRepository.getRoutinesBySemester(semester).collect { 
                routines.value = it
                isLoading.value = false
            }
        }
    }

    fun addRoutine(routine: Routine) {
        viewModelScope.launch { routineRepository.addRoutine(routine) }
    }

    fun updateRoutine(routine: Routine) {
        viewModelScope.launch { routineRepository.updateRoutine(routine) }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch { routineRepository.deleteRoutine(routineId) }
    }
}
