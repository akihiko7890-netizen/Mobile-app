package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_notes")
data class FamilyNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val childName: String = "Alex",
    val childAge: Int = 12,
    val type: String = NoteType.DAILY.name,
    val mood: String = MoodType.GOOD.name,
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis(),
    val bestPart: String = "",
    val difficultPart: String = "",
    val learned: String = "",
    val anythingElse: String = "",
    val attachedPhotoRes: Int = 0,
    val isDraft: Boolean = false,
    val isReadByParent: Boolean = false,
    val parentReactions: String = "", // Comma-separated emojis, e.g. "❤️,👏"
    val parentReply: String = "",
    val replyAuthor: String = "", // "Mom", "Dad"
    val replyTimestamp: Long = 0
)
