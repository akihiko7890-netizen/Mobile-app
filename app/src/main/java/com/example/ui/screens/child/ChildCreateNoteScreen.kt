package com.example.ui.screens.child

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MoodType
import com.example.data.model.NoteType
import com.example.ui.components.MoodSelector
import com.example.ui.theme.SoftPeach
import com.example.ui.theme.SoftSageGreen
import com.example.ui.theme.WarmNestOrange

@Composable
fun ChildCreateNoteScreen(
    noteType: NoteType,
    initialMood: MoodType,
    isSuccessScreen: Boolean,
    onDismissSuccess: () -> Unit,
    onSaveDraft: (best: String, diff: String, learned: String, extra: String) -> Unit,
    onSubmitNote: (best: String, diff: String, learned: String, extra: String, photoRes: Int) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSuccessScreen) {
        // Success celebration screen
        SuccessNoteScreen(onBackHome = onDismissSuccess)
        return
    }

    val totalSteps = if (noteType == NoteType.DAILY) 5 else 6

    var currentStep by remember { mutableIntStateOf(1) }
    var selectedMood by remember { mutableStateOf(initialMood) }
    var bestPartText by remember { mutableStateOf("") }
    var difficultPartText by remember { mutableStateOf("") }
    var learnedText by remember { mutableStateOf("") }
    var anythingElseText by remember { mutableStateOf("") }
    var attachedPhotoRes by remember { mutableIntStateOf(0) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val quickPrompts = listOf("🏫 School", "👫 Friends", "🏡 Family", "⚽ Sports", "🎨 Hobby", "😂 Something funny")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top Header with Back button, title, and step indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (currentStep > 1) currentStep-- else onCancel()
                },
                modifier = Modifier.testTag("create_note_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (noteType == NoteType.DAILY) "My Day" else "My Week",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "$currentStep of $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = {
                    onSaveDraft(bestPartText, difficultPartText, learnedText, anythingElseText)
                    onCancel()
                },
                modifier = Modifier.testTag("save_draft_button")
            ) {
                Text(
                    text = "Save Draft",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Progress Bar
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = WarmNestOrange,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Step Content Box
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "step_content"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (step) {
                        1 -> {
                            // Step 1: Mood selection
                            Text(
                                text = if (noteType == NoteType.DAILY) "How are you feeling today?" else "How would you describe this week overall?",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Pick the bird mood that matches your feelings right now.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            MoodSelector(
                                selectedMood = selectedMood,
                                onMoodSelected = { selectedMood = it }
                            )

                            // Mood explanation card
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = selectedMood.accentColor.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(text = selectedMood.emoji, fontSize = 28.sp)
                                    Column {
                                        Text(
                                            text = selectedMood.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = selectedMood.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Step 2: Best part of the day / week
                            Text(
                                text = if (noteType == NoteType.DAILY) "What was the best part of your day?" else "What made you happiest this week?",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Even little things count! A good lunch, a funny joke, or finishing a project.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Quick prompt pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                quickPrompts.take(3).forEach { prompt ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                if (bestPartText.isBlank()) bestPartText = "$prompt: "
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = prompt,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = bestPartText,
                                onValueChange = { bestPartText = it },
                                placeholder = {
                                    Text("Something that made me happy was...")
                                },
                                shape = RoundedCornerShape(18.dp),
                                minLines = 5,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = WarmNestOrange,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("best_part_input")
                            )
                        }

                        3 -> {
                            // Step 3: Difficult part (with optional SKIP)
                            Text(
                                text = if (noteType == NoteType.DAILY) "Did anything feel difficult today?" else "Was anything challenging this week?",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "You can tell your family anything you want them to know, or skip if you'd rather not share right now.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = difficultPartText,
                                onValueChange = { difficultPartText = it },
                                placeholder = {
                                    Text("Something that was tricky or felt hard was...")
                                },
                                shape = RoundedCornerShape(18.dp),
                                minLines = 4,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = WarmNestOrange,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("difficult_part_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(
                                    onClick = {
                                        difficultPartText = ""
                                        currentStep++
                                    },
                                    modifier = Modifier.testTag("skip_question_button")
                                ) {
                                    Text(
                                        text = "Skip this question →",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        4 -> {
                            // Step 4: What I learned
                            Text(
                                text = if (noteType == NoteType.DAILY) "What did you learn today?" else "What are you proud of learning this week?",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "It could be a new fact at school, a lesson from a friend, or how to do something new.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = learnedText,
                                onValueChange = { learnedText = it },
                                placeholder = {
                                    Text("Today I learned that...")
                                },
                                shape = RoundedCornerShape(18.dp),
                                minLines = 4,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = WarmNestOrange,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("learned_input")
                            )
                        }

                        5 -> {
                            // Step 5: Anything else + Optional Photo attachment
                            Text(
                                text = "Anything else you want to tell your family?",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "A question, something you need help with, or a sweet note to Mom and Dad.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = anythingElseText,
                                onValueChange = { anythingElseText = it },
                                placeholder = {
                                    Text("I wanted to tell you...")
                                },
                                shape = RoundedCornerShape(18.dp),
                                minLines = 3,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = WarmNestOrange,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("anything_else_input")
                            )

                            // Photo Attachment Section
                            Text(
                                text = "Attach a photo or drawing (optional):",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (attachedPhotoRes == R.drawable.img_note_in_nest) {
                                            SoftPeach
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            attachedPhotoRes = if (attachedPhotoRes == R.drawable.img_note_in_nest) 0 else R.drawable.img_note_in_nest
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_note_in_nest),
                                            contentDescription = "Note in nest photo",
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (attachedPhotoRes == R.drawable.img_note_in_nest) "Selected ✓" else "Add Nest Art",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (attachedPhotoRes == R.drawable.img_nest_welcome) {
                                            SoftPeach
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            attachedPhotoRes = if (attachedPhotoRes == R.drawable.img_nest_welcome) 0 else R.drawable.img_nest_welcome
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_nest_welcome),
                                            contentDescription = "Birds in nest",
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (attachedPhotoRes == R.drawable.img_nest_welcome) "Selected ✓" else "Add Family Photo",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        6 -> {
                            // Step 6: Weekly reflection review (if weekly)
                            Text(
                                text = "What are you looking forward to next week?",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Any events, sports, visits, or fun plans you're excited about?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = anythingElseText,
                                onValueChange = { anythingElseText = it },
                                placeholder = {
                                    Text("Next week I'm looking forward to...")
                                },
                                shape = RoundedCornerShape(18.dp),
                                minLines = 4,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = WarmNestOrange,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Bottom Controls: Back / Next or Send to Family
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("Back", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (currentStep < totalSteps) {
                Button(
                    onClick = { currentStep++ },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmNestOrange),
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("step_next_button")
                ) {
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Button(
                    onClick = { showConfirmDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmNestOrange),
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("send_to_family_button")
                ) {
                    Text(
                        text = "Send to My Family 🪺",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    // Confirmation Dialog before sending
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Text(text = "🪺", fontSize = 32.sp) },
            title = {
                Text(
                    text = "Ready to send your note?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Your family will be able to read this note in your shared nest.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onSubmitNote(
                            bestPartText,
                            difficultPartText,
                            learnedText,
                            anythingElseText,
                            attachedPhotoRes
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmNestOrange),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("confirm_send_note_button")
                ) {
                    Text("Send Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Keep Editing")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun SuccessNoteScreen(
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_note_in_nest),
            contentDescription = "Note in nest",
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(32.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Your note is in the nest! 🪺",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Your family can read it now. Mom and Dad will be notified and can leave a warm reply.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onBackHome,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WarmNestOrange),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("success_back_home_button")
        ) {
            Text(
                text = "Back Home",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
