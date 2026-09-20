package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FamilyMessage
import com.example.data.model.FamilyNote
import kotlinx.coroutines.flow.Flow

@Dao
interface NestNoteDao {

    @Query("SELECT * FROM family_notes WHERE isDraft = 0 ORDER BY timestamp DESC")
    fun getAllSubmittedNotes(): Flow<List<FamilyNote>>

    @Query("SELECT * FROM family_notes WHERE childName = :childName AND isDraft = 0 ORDER BY timestamp DESC")
    fun getSubmittedNotesByChild(childName: String): Flow<List<FamilyNote>>

    @Query("SELECT * FROM family_notes WHERE id = :id")
    fun getNoteById(id: Long): Flow<FamilyNote?>

    @Query("SELECT COUNT(*) FROM family_notes WHERE isDraft = 0 AND isReadByParent = 0")
    fun getUnreadNotesCount(): Flow<Int>

    @Query("SELECT * FROM family_notes WHERE isDraft = 1 ORDER BY timestamp DESC LIMIT 1")
    fun getLatestDraft(): Flow<FamilyNote?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: FamilyNote): Long

    @Update
    suspend fun updateNote(note: FamilyNote)

    @Query("UPDATE family_notes SET isReadByParent = 1 WHERE id = :id")
    suspend fun markNoteAsRead(id: Long)

    @Query("UPDATE family_notes SET parentReply = :reply, replyAuthor = :author, replyTimestamp = :timestamp WHERE id = :id")
    suspend fun addParentReply(id: Long, reply: String, author: String, timestamp: Long)

    @Query("UPDATE family_notes SET parentReactions = :reactions WHERE id = :id")
    suspend fun updateParentReactions(id: Long, reactions: String)

    @Query("DELETE FROM family_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    // Messages
    @Query("SELECT * FROM family_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<FamilyMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: FamilyMessage): Long
}
