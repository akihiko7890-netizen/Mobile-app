package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.SoftBlueAccent
import com.example.ui.theme.SoftRoseAccent
import com.example.ui.theme.SoftSageGreen
import com.example.ui.theme.WarmNestOrange

enum class MoodType(
    val emoji: String,
    val title: String,
    val description: String,
    val accentColor: Color
) {
    GREAT("😄", "Great", "Feeling super happy & energized", WarmNestOrange),
    GOOD("🙂", "Good", "Things went well today", SoftSageGreen),
    OKAY("😐", "Okay", "Just a normal, steady day", SoftBlueAccent),
    HARD("😔", "Hard", "Felt a bit down or challenged", SoftRoseAccent),
    TOUGH("😣", "Tough", "Had a rough time and need support", Color(0xFFE5989B));

    companion object {
        fun fromName(name: String): MoodType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: GOOD
        }
    }
}
