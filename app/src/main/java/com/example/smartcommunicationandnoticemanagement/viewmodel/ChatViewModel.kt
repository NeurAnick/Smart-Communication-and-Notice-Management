package com.example.smartcommunicationandnoticemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcommunicationandnoticemanagement.data.model.Message
import com.example.smartcommunicationandnoticemanagement.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    val isSending = MutableStateFlow(false)

    fun loadMessages(semester: Int) {
        viewModelScope.launch {
            chatRepository.getMessages(semester).collect { _messages.value = it }
        }
    }

    fun sendMessage(semester: Int, senderId: String, senderName: String, text: String) {
        viewModelScope.launch {
            isSending.value = true
            val msg = Message(
                senderId = senderId,
                senderName = senderName,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            chatRepository.sendMessage(semester, msg)
            isSending.value = false
        }
    }
}
