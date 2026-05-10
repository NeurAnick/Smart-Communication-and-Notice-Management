package com.example.smartcommunicationandnoticemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcommunicationandnoticemanagement.data.model.User
import com.example.smartcommunicationandnoticemanagement.data.repository.AuthRepository
import com.example.smartcommunicationandnoticemanagement.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class RegisterSuccess(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()

    // Helper to get user from any authenticated state
    val currentUser: User? get() = when (this) {
        is Authenticated -> user
        is RegisterSuccess -> user
        else -> null
    }
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun resetState() {
        _authState.value = AuthState.Idle
        _errorMessage.value = null
        _email.value = ""
        _password.value = ""
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(email, pass)
            result.onSuccess {
                checkCurrentUser()
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
        }
    }

    fun register(
        email: String,
        pass: String,
        fullName: String,
        department: String,
        semester: Int,
        studentId: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.register(email, pass, fullName, department, semester, studentId)
            result.onSuccess { user ->
                val savedUser = userRepository.getUser(user.uid)
                if (savedUser != null) {
                    _authState.value = AuthState.RegisterSuccess(savedUser)
                } else {
                    _authState.value = AuthState.RegisterSuccess(user)
                }
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Registration failed")
            }
        }
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            try {
                val user = userRepository.getUser(uid)
                if (user != null) {
                    _authState.value = AuthState.Authenticated(user)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun checkCurrentUser() {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                _authState.value = AuthState.Unauthenticated
                return@launch
            }
            try {
                val user = userRepository.getUser(currentUser.uid)
                if (user != null) {
                    _authState.value = AuthState.Authenticated(user)
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun sendPasswordReset(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val res = authRepository.sendPasswordResetEmail(email)
            if (res.isSuccess) {
                _authState.value = AuthState.Idle
                onResult(true)
            } else {
                _authState.value = AuthState.Error(res.exceptionOrNull()?.message ?: "Reset failed")
                onResult(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _email.value = ""
            _password.value = ""
            _authState.value = AuthState.Unauthenticated
            _errorMessage.value = null
        }
    }
}
