package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.NestNoteDatabase
import com.example.data.model.NoteType
import com.example.data.model.UserRole
import com.example.data.repository.NestNoteRepository
import com.example.ui.components.NestBottomBar
import com.example.ui.components.NestTopBar
import com.example.ui.screens.FamilyCodeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.child.ChildCreateNoteScreen
import com.example.ui.screens.child.ChildHomeScreen
import com.example.ui.screens.child.ChildMessagesScreen
import com.example.ui.screens.child.ChildNoteDetailScreen
import com.example.ui.screens.child.ChildNotesHistoryScreen
import com.example.ui.screens.child.ChildProfileScreen
import com.example.ui.screens.parent.OurNestTimelineScreen
import com.example.ui.screens.parent.ParentFamilyNotesScreen
import com.example.ui.screens.parent.ParentHomeScreen
import com.example.ui.screens.parent.ParentMoodInsightsScreen
import com.example.ui.screens.parent.ParentProfileScreen
import com.example.ui.screens.parent.ParentReportReaderScreen
import com.example.ui.theme.NestNoteTheme
import com.example.ui.viewmodel.NestNoteViewModel
import com.example.ui.viewmodel.NestNoteViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = NestNoteDatabase.getDatabase(applicationContext)
        val repository = NestNoteRepository(db.nestNoteDao())
        val factory = NestNoteViewModelFactory(repository)

        setContent {
            val viewModel: NestNoteViewModel = viewModel(factory = factory)
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            NestNoteTheme(darkTheme = isDarkMode) {
                NestNoteApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NestNoteApp(viewModel: NestNoteViewModel) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val isFamilyConnected by viewModel.isFamilyConnected.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val activeChildName by viewModel.activeChildName.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val nestCode by viewModel.nestCode.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()

    val allNotes by viewModel.allNotes.collectAsState()
    val unreadNotesCount by viewModel.unreadNotesCount.collectAsState()
    val allMessages by viewModel.allMessages.collectAsState()

    val noteTypeBeingCreated by viewModel.noteTypeBeingCreated.collectAsState()
    val draftMood by viewModel.draftMood.collectAsState()
    val noteSubmittedSuccess by viewModel.noteSubmittedSuccess.collectAsState()
    val uiNotice by viewModel.uiNotice.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiNotice) {
        uiNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.clearNotice()
        }
    }

    // Onboarding Sub-steps: 0 = Slides, 1 = Role Choice, 2 = Family Code
    var onboardingStep by remember { mutableStateOf(0) }

    // Screen routes:
    // Child: "child_home", "child_notes", "child_create_note", "child_note_detail", "child_messages", "child_profile"
    // Parent: "parent_home", "parent_notes", "parent_reader", "our_nest", "mood_insights", "parent_profile"
    var currentScreen by remember { mutableStateOf("child_home") }
    var selectedNoteId by remember { mutableStateOf<Long?>(null) }

    // Keep screen route in sync when role toggles
    LaunchedEffect(currentRole) {
        if (currentRole == UserRole.CHILD && !currentScreen.startsWith("child_")) {
            currentScreen = "child_home"
        } else if (currentRole == UserRole.PARENT && !currentScreen.startsWith("parent_") && currentScreen != "our_nest" && currentScreen != "mood_insights") {
            currentScreen = "parent_home"
        }
    }

    // Handle back presses smoothly
    BackHandler(enabled = currentScreen == "child_create_note" || currentScreen == "child_note_detail" || currentScreen == "parent_reader") {
        if (currentScreen == "child_create_note" || currentScreen == "child_note_detail") {
            currentScreen = "child_home"
        } else if (currentScreen == "parent_reader") {
            currentScreen = "parent_home"
        }
    }

    if (!isOnboardingCompleted) {
        when (onboardingStep) {
            0 -> {
                OnboardingScreen(onFinished = { onboardingStep = 1 })
            }
            1 -> {
                RoleSelectionScreen(
                    onRoleSelected = { role ->
                        viewModel.setRole(role)
                        onboardingStep = 2
                    }
                )
            }
            2 -> {
                FamilyCodeScreen(
                    currentCode = nestCode,
                    onCodeConfirmed = { code ->
                        viewModel.connectWithCode(code)
                    },
                    onSkipToDemo = {
                        viewModel.completeOnboarding()
                    }
                )
            }
        }
        return
    }

    val showBottomBar = currentScreen in listOf(
        "child_home", "child_notes", "child_messages", "child_profile",
        "parent_home", "parent_notes", "our_nest", "mood_insights", "parent_profile"
    )

    val showTopBar = currentScreen != "child_create_note"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showTopBar) {
                NestTopBar(
                    currentRole = currentRole,
                    activeChildName = activeChildName,
                    isDarkMode = isDarkMode,
                    onToggleRole = { viewModel.toggleRole() },
                    onToggleDarkMode = { viewModel.toggleDarkMode() }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NestBottomBar(
                    currentRole = currentRole,
                    currentScreen = currentScreen,
                    unreadNotesCount = unreadNotesCount,
                    onNavigate = { route -> currentScreen = route }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    // CHILD SCREENS
                    "child_home" -> {
                        val latestWithReply = allNotes.firstOrNull { it.childName == activeChildName && it.parentReply.isNotBlank() }
                        ChildHomeScreen(
                            childName = activeChildName,
                            selectedMood = draftMood,
                            onMoodSelected = { viewModel.setDraftMood(it) },
                            onWriteDailyNote = {
                                viewModel.startCreatingNote(NoteType.DAILY)
                                currentScreen = "child_create_note"
                            },
                            onWriteWeeklyNote = {
                                viewModel.startCreatingNote(NoteType.WEEKLY)
                                currentScreen = "child_create_note"
                            },
                            onOpenNoteDetail = { noteId ->
                                selectedNoteId = noteId
                                currentScreen = "child_note_detail"
                            },
                            latestNoteWithReply = latestWithReply
                        )
                    }

                    "child_create_note" -> {
                        ChildCreateNoteScreen(
                            noteType = noteTypeBeingCreated,
                            initialMood = draftMood,
                            isSuccessScreen = noteSubmittedSuccess,
                            onDismissSuccess = {
                                viewModel.dismissSuccessScreen()
                                currentScreen = "child_home"
                            },
                            onSaveDraft = { best, diff, learned, extra ->
                                viewModel.saveDraftNote(best, diff, learned, extra)
                            },
                            onSubmitNote = { best, diff, learned, extra, photoRes ->
                                viewModel.submitNote(best, diff, learned, extra, photoRes)
                            },
                            onCancel = { currentScreen = "child_home" }
                        )
                    }

                    "child_notes" -> {
                        ChildNotesHistoryScreen(
                            childName = activeChildName,
                            notes = allNotes,
                            onOpenNoteDetail = { noteId ->
                                selectedNoteId = noteId
                                currentScreen = "child_note_detail"
                            },
                            onWriteNewNote = {
                                viewModel.startCreatingNote(NoteType.DAILY)
                                currentScreen = "child_create_note"
                            }
                        )
                    }

                    "child_note_detail" -> {
                        val note = allNotes.find { it.id == selectedNoteId } ?: allNotes.firstOrNull()
                        if (note != null) {
                            ChildNoteDetailScreen(
                                note = note,
                                onBack = { currentScreen = "child_home" },
                                onSendQuickResponse = { response ->
                                    viewModel.sendFamilyMessage(
                                        content = response,
                                        senderName = activeChildName,
                                        senderRole = UserRole.CHILD,
                                        recipient = "Parents"
                                    )
                                },
                                onDeleteNote = {
                                    viewModel.deleteNote(note.id)
                                    currentScreen = "child_notes"
                                }
                            )
                        } else {
                            currentScreen = "child_home"
                        }
                    }

                    "child_messages" -> {
                        ChildMessagesScreen(
                            childName = activeChildName,
                            messages = allMessages,
                            onSendMessage = { content ->
                                viewModel.sendFamilyMessage(
                                    content = content,
                                    senderName = activeChildName,
                                    senderRole = UserRole.CHILD,
                                    recipient = "Parents"
                                )
                            }
                        )
                    }

                    "child_profile" -> {
                        ChildProfileScreen(
                            childName = activeChildName,
                            nestCode = nestCode,
                            familyMembers = familyMembers,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            onSwitchToParentView = {
                                viewModel.setRole(UserRole.PARENT)
                                currentScreen = "parent_home"
                            }
                        )
                    }

                    // PARENT SCREENS
                    "parent_home" -> {
                        ParentHomeScreen(
                            parentName = "Sarah",
                            familyMembers = familyMembers,
                            notes = allNotes,
                            onOpenNoteReader = { noteId ->
                                selectedNoteId = noteId
                                currentScreen = "parent_reader"
                            },
                            onViewAllNotes = { currentScreen = "parent_notes" },
                            onSelectChildFilter = { _ -> currentScreen = "parent_notes" }
                        )
                    }

                    "parent_notes" -> {
                        ParentFamilyNotesScreen(
                            notes = allNotes,
                            onOpenNoteReader = { noteId ->
                                selectedNoteId = noteId
                                currentScreen = "parent_reader"
                            }
                        )
                    }

                    "parent_reader" -> {
                        val note = allNotes.find { it.id == selectedNoteId } ?: allNotes.firstOrNull()
                        if (note != null) {
                            ParentReportReaderScreen(
                                note = note,
                                onBack = { currentScreen = "parent_home" },
                                onMarkAsRead = { viewModel.markNoteRead(note.id) },
                                onSendReply = { reply, author ->
                                    viewModel.sendParentReply(note.id, reply, author)
                                },
                                onSelectReaction = { emoji ->
                                    viewModel.addParentReaction(note.id, emoji, note.parentReactions)
                                }
                            )
                        } else {
                            currentScreen = "parent_home"
                        }
                    }

                    "our_nest" -> {
                        OurNestTimelineScreen(
                            notes = allNotes,
                            onOpenNoteReader = { noteId ->
                                selectedNoteId = noteId
                                currentScreen = "parent_reader"
                            }
                        )
                    }

                    "mood_insights" -> {
                        ParentMoodInsightsScreen(notes = allNotes)
                    }

                    "parent_profile" -> {
                        ParentProfileScreen(
                            parentName = "Sarah",
                            nestCode = nestCode,
                            familyMembers = familyMembers,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            onGenerateNewCode = { viewModel.generateNewNestCode() },
                            onAddChild = { name, age -> viewModel.addChildMember(name, age) },
                            onSwitchToChildView = {
                                viewModel.setRole(UserRole.CHILD)
                                currentScreen = "child_home"
                            }
                        )
                    }

                    else -> {
                        ChildHomeScreen(
                            childName = activeChildName,
                            selectedMood = draftMood,
                            onMoodSelected = { viewModel.setDraftMood(it) },
                            onWriteDailyNote = {
                                viewModel.startCreatingNote(NoteType.DAILY)
                                currentScreen = "child_create_note"
                            },
                            onWriteWeeklyNote = {
                                viewModel.startCreatingNote(NoteType.WEEKLY)
                                currentScreen = "child_create_note"
                            },
                            onOpenNoteDetail = { noteId ->
                                selectedNoteId = noteId
                                currentScreen = "child_note_detail"
                            },
                            latestNoteWithReply = null
                        )
                    }
                }
            }
        }
    }
}
