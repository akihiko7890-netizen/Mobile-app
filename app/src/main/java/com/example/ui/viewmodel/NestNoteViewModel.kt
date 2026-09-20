package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.FamilyMember
import com.example.data.model.FamilyMessage
import com.example.data.model.FamilyNote
import com.example.data.model.MoodType
import com.example.data.model.NoteType
import com.example.data.model.UserRole
import com.example.data.repository.NestNoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NestNoteViewModel(
    private val repository: NestNoteRepository
) : ViewModel() {

    // App Navigation & Session state
    private val _currentRole = MutableStateFlow(UserRole.CHILD)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isFamilyConnected = MutableStateFlow(true)
    val isFamilyConnected: StateFlow<Boolean> = _isFamilyConnected.asStateFlow()

    private val _nestCode = MutableStateFlow("AB7K92")
    val nestCode: StateFlow<String> = _nestCode.asStateFlow()

    private val _activeChildName = MutableStateFlow("Alex")
    val activeChildName: StateFlow<String> = _activeChildName.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Family Members
    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

    // Reactive database streams
    val allNotes: StateFlow<List<FamilyNote>> = repository.allSubmittedNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotesCount: StateFlow<Int> = repository.unreadCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allMessages: StateFlow<List<FamilyMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestDraft: StateFlow<FamilyNote?> = repository.latestDraft
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Note creation active wizard state
    private val _draftMood = MutableStateFlow<MoodType>(MoodType.GOOD)
    val draftMood: StateFlow<MoodType> = _draftMood.asStateFlow()

    private val _draftStep = MutableStateFlow(1)
    val draftStep: StateFlow<Int> = _draftStep.asStateFlow()

    private val _noteTypeBeingCreated = MutableStateFlow(NoteType.DAILY)
    val noteTypeBeingCreated: StateFlow<NoteType> = _noteTypeBeingCreated.asStateFlow()

    private val _noteSubmittedSuccess = MutableStateFlow(false)
    val noteSubmittedSuccess: StateFlow<Boolean> = _noteSubmittedSuccess.asStateFlow()

    // Notification toast/message for UI feedback
    private val _uiNotice = MutableStateFlow<String?>(null)
    val uiNotice: StateFlow<String?> = _uiNotice.asStateFlow()

    init {
        _familyMembers.value = repository.getInitialFamilyMembers()
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    fun toggleRole() {
        _currentRole.value = if (_currentRole.value == UserRole.CHILD) UserRole.PARENT else UserRole.CHILD
    }

    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
    }

    fun connectWithCode(code: String): Boolean {
        return if (code.trim().equals(_nestCode.value, ignoreCase = true) || code.isNotBlank()) {
            _isFamilyConnected.value = true
            _isOnboardingCompleted.value = true
            true
        } else {
            false
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setActiveChildName(name: String) {
        _activeChildName.value = name
    }

    fun startCreatingNote(type: NoteType) {
        _noteTypeBeingCreated.value = type
        _draftStep.value = 1
        _draftMood.value = MoodType.GOOD
        _noteSubmittedSuccess.value = false
    }

    fun setDraftMood(mood: MoodType) {
        _draftMood.value = mood
    }

    fun setDraftStep(step: Int) {
        _draftStep.value = step
    }

    fun submitNote(
        bestPart: String,
        difficultPart: String,
        learned: String,
        anythingElse: String,
        attachedPhoto: Int = 0,
        type: NoteType = _noteTypeBeingCreated.value
    ) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            val dateLabel = sdf.format(Date())

            val newNote = FamilyNote(
                childName = _activeChildName.value,
                childAge = if (_activeChildName.value == "Alex") 12 else 9,
                type = type.name,
                mood = _draftMood.value.name,
                dateString = dateLabel,
                timestamp = System.currentTimeMillis(),
                bestPart = bestPart,
                difficultPart = difficultPart,
                learned = learned,
                anythingElse = anythingElse,
                attachedPhotoRes = attachedPhoto,
                isDraft = false,
                isReadByParent = false,
                parentReactions = "",
                parentReply = "",
                replyAuthor = "",
                replyTimestamp = 0
            )
            repository.saveNote(newNote)
            _noteSubmittedSuccess.value = true
            _uiNotice.value = "Your note is in the nest! 🪺"
        }
    }

    fun saveDraftNote(
        bestPart: String,
        difficultPart: String,
        learned: String,
        anythingElse: String,
        type: NoteType = _noteTypeBeingCreated.value
    ) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            val dateLabel = "Draft • " + sdf.format(Date())

            val draft = FamilyNote(
                childName = _activeChildName.value,
                childAge = 12,
                type = type.name,
                mood = _draftMood.value.name,
                dateString = dateLabel,
                timestamp = System.currentTimeMillis(),
                bestPart = bestPart,
                difficultPart = difficultPart,
                learned = learned,
                anythingElse = anythingElse,
                isDraft = true,
                isReadByParent = false
            )
            repository.saveNote(draft)
            _uiNotice.value = "Draft saved safely in your nest 🌿"
        }
    }

    fun dismissSuccessScreen() {
        _noteSubmittedSuccess.value = false
    }

    fun markNoteRead(noteId: Long) {
        viewModelScope.launch {
            repository.markNoteAsRead(noteId)
        }
    }

    fun sendParentReply(noteId: Long, replyText: String, author: String = "Mom") {
        if (replyText.isBlank()) return
        viewModelScope.launch {
            repository.addParentReply(noteId, replyText.trim(), author)
            _uiNotice.value = "Your reply was sent ❤️"
        }
    }

    fun addParentReaction(noteId: Long, reactionEmoji: String, existingReactions: String) {
        viewModelScope.launch {
            repository.addReaction(noteId, reactionEmoji, existingReactions)
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            _uiNotice.value = "Note removed"
        }
    }

    fun sendFamilyMessage(content: String, senderName: String, senderRole: UserRole, recipient: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val message = FamilyMessage(
                senderName = senderName,
                senderRole = senderRole.name,
                recipientName = recipient,
                content = content.trim(),
                timestamp = System.currentTimeMillis()
            )
            repository.sendMessage(message)
        }
    }

    fun clearNotice() {
        _uiNotice.value = null
    }

    fun addChildMember(name: String, age: Int) {
        val newChild = FamilyMember(
            id = System.currentTimeMillis().toString(),
            name = name,
            role = UserRole.CHILD,
            relation = "Child",
            age = age,
            avatarEmoji = if (age % 2 == 0) "👦" else "👧",
            isConnected = true
        )
        _familyMembers.value = _familyMembers.value + newChild
        _uiNotice.value = "$name was added to the nest!"
    }

    fun generateNewNestCode() {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = (1..6).map { chars.random() }.joinToString("")
        _nestCode.value = code
        _uiNotice.value = "New Nest Code generated: $code"
    }
}

class NestNoteViewModelFactory(
    private val repository: NestNoteRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NestNoteViewModel::class.java)) {
            return NestNoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
