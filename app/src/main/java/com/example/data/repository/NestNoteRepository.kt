package com.example.data.repository

import com.example.R
import com.example.data.local.NestNoteDao
import com.example.data.model.FamilyMember
import com.example.data.model.FamilyMessage
import com.example.data.model.FamilyNote
import com.example.data.model.MoodType
import com.example.data.model.NoteType
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NestNoteRepository(private val dao: NestNoteDao) {

    val allSubmittedNotes: Flow<List<FamilyNote>> = dao.getAllSubmittedNotes()
    val unreadCount: Flow<Int> = dao.getUnreadNotesCount()
    val allMessages: Flow<List<FamilyMessage>> = dao.getAllMessages()
    val latestDraft: Flow<FamilyNote?> = dao.getLatestDraft()

    fun getNotesByChild(childName: String): Flow<List<FamilyNote>> =
        dao.getSubmittedNotesByChild(childName)

    fun getNoteById(id: Long): Flow<FamilyNote?> =
        dao.getNoteById(id)

    suspend fun saveNote(note: FamilyNote): Long {
        return dao.insertNote(note)
    }

    suspend fun markNoteAsRead(id: Long) {
        dao.markNoteAsRead(id)
    }

    suspend fun addParentReply(id: Long, reply: String, author: String) {
        dao.addParentReply(id, reply, author, System.currentTimeMillis())
        dao.markNoteAsRead(id)
    }

    suspend fun addReaction(id: Long, reaction: String, existingReactions: String) {
        val list = if (existingReactions.isBlank()) {
            listOf(reaction)
        } else {
            val current = existingReactions.split(",").map { it.trim() }.toMutableList()
            if (!current.contains(reaction)) {
                current.add(reaction)
            }
            current
        }
        dao.updateParentReactions(id, list.joinToString(","))
    }

    suspend fun deleteNote(id: Long) {
        dao.deleteNoteById(id)
    }

    suspend fun sendMessage(message: FamilyMessage): Long {
        return dao.insertMessage(message)
    }

    suspend fun seedInitialDataIfEmpty() {
        val existing = dao.getAllSubmittedNotes().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 24L * 60L * 60L * 1000L

            val note1 = FamilyNote(
                childName = "Alex",
                childAge = 12,
                type = NoteType.DAILY.name,
                mood = MoodType.GOOD.name,
                dateString = "Yesterday",
                timestamp = now - (dayMs * 1),
                bestPart = "We played soccer at school after lunch and scored a goal! Also had hot cocoa.",
                difficultPart = "Math homework had long division fractions that took me a while.",
                learned = "How volcanoes form tectonic pressure deep under islands.",
                anythingElse = "Can we visit grandpa this weekend?",
                attachedPhotoRes = R.drawable.img_note_in_nest,
                isDraft = false,
                isReadByParent = true,
                parentReactions = "❤️,👏",
                parentReply = "I'm so proud of you for sticking with your math fractions! And yes, we can definitely call grandpa tonight ❤️",
                replyAuthor = "Mom",
                replyTimestamp = now - (dayMs * 1) + 3600000L
            )

            val note2 = FamilyNote(
                childName = "Jamie",
                childAge = 9,
                type = NoteType.DAILY.name,
                mood = MoodType.GREAT.name,
                dateString = "Today, 3:15 PM",
                timestamp = now - (1800000L), // 30 mins ago
                bestPart = "Art class was painting clay birds. Mine has bright blue wings!",
                difficultPart = "",
                learned = "Mixing yellow and blue makes any forest green shade.",
                anythingElse = "I left it in the art room drying rack!",
                attachedPhotoRes = R.drawable.img_nest_welcome,
                isDraft = false,
                isReadByParent = false,
                parentReactions = "",
                parentReply = "",
                replyAuthor = "",
                replyTimestamp = 0
            )

            val note3 = FamilyNote(
                childName = "Alex",
                childAge = 12,
                type = NoteType.WEEKLY.name,
                mood = MoodType.GOOD.name,
                dateString = "Sunday Reflection",
                timestamp = now - (dayMs * 4),
                bestPart = "Finishing my science poster board project and presenting to class without stuttering.",
                difficultPart = "Waking up early on Wednesday when it was freezing cold outside.",
                learned = "How gravity keeps planets orbiting together safely.",
                anythingElse = "Looking forward to robotics club next Tuesday!",
                attachedPhotoRes = 0,
                isDraft = false,
                isReadByParent = true,
                parentReactions = "👏,⭐",
                parentReply = "You gave such a confident presentation Alex, we love seeing you shine!",
                replyAuthor = "Dad",
                replyTimestamp = now - (dayMs * 4) + 7200000L
            )

            dao.insertNote(note1)
            dao.insertNote(note2)
            dao.insertNote(note3)

            // Seed messages
            dao.insertMessage(
                FamilyMessage(
                    senderName = "Mom",
                    senderRole = UserRole.PARENT.name,
                    recipientName = "Alex",
                    content = "Good morning sweetheart! Remember you have soccer practice at 4:30 PM today. Have a wonderful day at school! 🌿",
                    timestamp = now - (dayMs * 1) + 28800000L,
                    reactionEmoji = "❤️"
                )
            )
            dao.insertMessage(
                FamilyMessage(
                    senderName = "Alex",
                    senderRole = UserRole.CHILD.name,
                    recipientName = "Mom",
                    content = "Thanks Mom! Packed my cleats in my bag. Love you! 😄",
                    timestamp = now - (dayMs * 1) + 29400000L,
                    reactionEmoji = "🤗"
                )
            )
            dao.insertMessage(
                FamilyMessage(
                    senderName = "Mom",
                    senderRole = UserRole.PARENT.name,
                    recipientName = "Alex",
                    content = "I saw your note yesterday about math. We can look at the fractions together after dinner if you'd like! You're doing great! ❤️",
                    timestamp = now - 3600000L,
                    reactionEmoji = "❤️"
                )
            )
        }
    }

    fun getInitialFamilyMembers(): List<FamilyMember> {
        return listOf(
            FamilyMember(
                id = "1",
                name = "Sarah",
                role = UserRole.PARENT,
                relation = "Mom",
                avatarEmoji = "👩",
                isConnected = true
            ),
            FamilyMember(
                id = "2",
                name = "David",
                role = UserRole.PARENT,
                relation = "Dad",
                avatarEmoji = "👨",
                isConnected = true
            ),
            FamilyMember(
                id = "3",
                name = "Alex",
                role = UserRole.CHILD,
                relation = "Son",
                age = 12,
                avatarEmoji = "👦",
                isConnected = true
            ),
            FamilyMember(
                id = "4",
                name = "Jamie",
                role = UserRole.CHILD,
                relation = "Daughter",
                age = 9,
                avatarEmoji = "👧",
                isConnected = true
            )
        )
    }

    companion object {
        fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
