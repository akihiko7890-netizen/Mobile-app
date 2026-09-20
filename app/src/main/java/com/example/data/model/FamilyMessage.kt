package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_messages")
data class FamilyMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderName: String,
    val senderRole: String, // CHILD or PARENT
    val recipientName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reactionEmoji: String = ""
)
