package com.example.smartcommunicationandnoticemanagement.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcommunicationandnoticemanagement.data.model.Notice
import com.example.smartcommunicationandnoticemanagement.data.repository.NoticeRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

sealed class PostState {
    object Idle : PostState()
    object Loading : PostState()
    object Success : PostState()
    data class Error(val message: String) : PostState()
}

sealed class EditState {
    object Idle : EditState()
    object Loading : EditState()
    object Success : EditState()
    data class Error(val message: String) : EditState()
}

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _savedNoticeIds = MutableStateFlow<List<String>>(emptyList())
    val savedNoticeIds: StateFlow<List<String>> = _savedNoticeIds.asStateFlow()

    private val _postState = MutableStateFlow<PostState>(PostState.Idle)
    val postState: StateFlow<PostState> = _postState.asStateFlow()

    private val _editState = MutableStateFlow<EditState>(EditState.Idle)
    val editState: StateFlow<EditState> = _editState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadNotices()
    }

    private fun loadNotices() {
        viewModelScope.launch {
            noticeRepository.getNoticesRealtime().collect { _notices.value = it }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500)
            _isRefreshing.value = false
        }
    }

    fun loadSavedNotices(userId: String) {
        viewModelScope.launch {
            noticeRepository.getSavedNoticeIds(userId).collect { _savedNoticeIds.value = it }
        }
    }

    fun postNotice(notice: Notice) {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            val result = noticeRepository.postNotice(notice)
            result.fold(
                onSuccess = { _postState.value = PostState.Success },
                onFailure = { _postState.value = PostState.Error(it.message ?: "Failed to post notice") }
            )
        }
    }

    fun updateNotice(notice: Notice) {
        viewModelScope.launch {
            _editState.value = EditState.Loading
            val result = noticeRepository.updateNotice(notice)
            result.fold(
                onSuccess = { _editState.value = EditState.Success },
                onFailure = { _editState.value = EditState.Error(it.message ?: "Update failed") }
            )
        }
    }

    suspend fun uploadImage(uri: Uri): String {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("notice_images/${UUID.randomUUID()}.jpg")
            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            ""
        }
    }

    fun resetPostState() {
        _postState.value = PostState.Idle
    }

    fun resetEditState() {
        _editState.value = EditState.Idle
    }

    fun deleteNotice(noticeId: String) {
        viewModelScope.launch { noticeRepository.deleteNotice(noticeId) }
    }

    fun getNoticeById(noticeId: String): Notice? {
        return _notices.value.find { it.noticeId == noticeId }
    }

    fun markNoticeSeen(noticeId: String, userId: String) {
        viewModelScope.launch { noticeRepository.markNoticeSeen(noticeId, userId) }
    }

    fun toggleSave(userId: String, noticeId: String, isSaved: Boolean) {
        viewModelScope.launch { noticeRepository.toggleSaveNotice(userId, noticeId, isSaved) }
    }

    fun getSeenCount(noticeId: String): Flow<Int> {
        return noticeRepository.getSeenByList(noticeId).map { it.size }
    }

    fun getSeenByList(noticeId: String) = noticeRepository.getSeenByList(noticeId)
}
