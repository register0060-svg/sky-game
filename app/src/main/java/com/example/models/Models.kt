package com.example.models

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GameDifficulty(
    val id: String,
    val title: String,
    val emoji: String,
    val timeMod: Int,
    val penalty: Int,
    val scoreMultiplier: Float,
    val color: Color,
    val bg: Color,
    val description: String
) {
    EASY("easy", "Оңай", "🟢", 8, 1, 1.0f, Color(0xFF10B981), Color(0x1410B981), "Уақыт +8с · Жаза −1с"),
    MEDIUM("medium", "Орташа", "🟡", 0, 2, 1.6f, Color(0xFFF59E0B), Color(0x14F59E0B), "Стандарт · Жаза −2с"),
    HARD("hard", "Қиын", "🔴", -10, 4, 2.8f, Color(0xFFEF4444), Color(0x14EF4444), "Уақыт −10с · Жаза −4с");
}

enum class GameMode(
    val id: Int,
    val title: String,
    val emoji: String,
    val numbersCount: Int,
    val baseTimeSeconds: Int,
    val columns: Int,
    val color: Color,
    val isChaos: Boolean
) {
    MODE_1(1, "Ұшқын", "✨", 16, 35, 4, Color(0xFFFCD34D), false),
    MODE_2(2, "Самал", "🌬️", 20, 35, 5, Color(0xFF34D399), false),
    MODE_3(3, "Шапақ", "🌅", 25, 35, 5, Color(0xFF60A5FA), false),
    MODE_4(4, "Сәуле", "🔆", 30, 38, 6, Color(0xFFF59E0B), false),
    MODE_5(5, "Серпін", "💨", 36, 42, 6, Color(0xFF38BDF8), false),
    MODE_6(6, "Найза", "⚡", 42, 48, 7, Color(0xFFEF4444), true),
    MODE_7(7, "Дауыл", "🌩️", 49, 55, 7, Color(0xFF8B5CF6), true),
    MODE_8(8, "Бұрқасын", "🌪️", 56, 62, 7, Color(0xFFEC4899), true),
    MODE_9(9, "Ғалам", "🌙", 64, 70, 8, Color(0xFFA78BFA), true),
    MODE_10(10, "Шексіздік", "🌌", 81, 90, 9, Color(0xFFF0ABFC), true);
    
    companion object {
        fun fromId(id: Int) = entries.find { it.id == id } ?: MODE_1
    }
}

@Entity(tableName = "scores")
data class ScoreEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val score: Int,
    val modeId: Int,
    val modeName: String,
    val diffId: String,
    val diffName: String,
    val time: Float,
    val mistakes: Int,
    val timestamp: Long = System.currentTimeMillis()
)
