package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FamilyMessage
import com.example.data.model.FamilyNote

@Database(
    entities = [FamilyNote::class, FamilyMessage::class],
    version = 1,
    exportSchema = false
)
abstract class NestNoteDatabase : RoomDatabase() {

    abstract fun nestNoteDao(): NestNoteDao

    companion object {
        @Volatile
        private var INSTANCE: NestNoteDatabase? = null

        fun getDatabase(context: Context): NestNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NestNoteDatabase::class.java,
                    "nest_note_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
