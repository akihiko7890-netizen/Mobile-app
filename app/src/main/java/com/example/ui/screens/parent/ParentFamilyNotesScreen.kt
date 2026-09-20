package com.example.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyNote
import com.example.data.model.NoteType
import com.example.ui.components.ReportCardItem
import com.example.ui.theme.WarmNestOrange

@Composable
fun ParentFamilyNotesScreen(
    notes: List<FamilyNote>,
    onOpenNoteReader: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedChildFilter by remember { mutableStateOf("All") }
    var selectedTypeFilter by remember { mutableStateOf("All") }
    var onlyUnread by remember { mutableStateOf(false) }

    val filteredNotes = notes.filter { note ->
        val matchesSearch = if (searchQuery.isBlank()) true else {
            note.bestPart.contains(searchQuery, ignoreCase = true) ||
            note.difficultPart.contains(searchQuery, ignoreCase = true) ||
            note.learned.contains(searchQuery, ignoreCase = true) ||
            note.childName.contains(searchQuery, ignoreCase = true) ||
            note.dateString.contains(searchQuery, ignoreCase = true)
        }
        val matchesChild = if (selectedChildFilter == "All") true else note.childName.equals(selectedChildFilter, ignoreCase = true)
        val matchesType = if (selectedTypeFilter == "All") true else note.type.equals(selectedTypeFilter, ignoreCase = true)
        val matchesUnread = if (!onlyUnread) true else !note.isReadByParent

        matchesSearch && matchesChild && matchesType && matchesUnread
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Family Notes",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Every day's joys, lessons, and questions from your kids",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by topic, keyword, or day...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = WarmNestOrange,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("parent_search_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedChildFilter == "All",
                    onClick = { selectedChildFilter = "All" },
                    label = { Text("All Children") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedChildFilter == "Alex",
                    onClick = { selectedChildFilter = "Alex" },
                    label = { Text("👦 Alex") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedChildFilter == "Jamie",
                    onClick = { selectedChildFilter = "Jamie" },
                    label = { Text("👧 Jamie") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
            item {
                FilterChip(
                    selected = onlyUnread,
                    onClick = { onlyUnread = !onlyUnread },
                    label = { Text("Unread Only 🔴") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WarmNestOrange.copy(alpha = 0.25f)
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedTypeFilter == NoteType.DAILY.name,
                    onClick = {
                        selectedTypeFilter = if (selectedTypeFilter == NoteType.DAILY.name) "All" else NoteType.DAILY.name
                    },
                    label = { Text("Daily") }
                )
            }
            item {
                FilterChip(
                    selected = selectedTypeFilter == NoteType.WEEKLY.name,
                    onClick = {
                        selectedTypeFilter = if (selectedTypeFilter == NoteType.WEEKLY.name) "All" else NoteType.WEEKLY.name
                    },
                    label = { Text("Weekly") }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredNotes.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🔍", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No notes found matching your filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Try clearing your search or switching child filters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    ReportCardItem(
                        note = note,
                        onClick = { onOpenNoteReader(note.id) },
                        showChildName = true
                    )
                }
            }
        }
    }
}
