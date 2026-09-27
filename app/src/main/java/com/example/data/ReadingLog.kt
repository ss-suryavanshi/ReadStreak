package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_logs",
    indices = [Index(value = ["dateString"], unique = true)]
)
data class ReadingLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dateString: String, // format "YYYY-MM-DD"
    val minutesRead: Int = 20,
    val pagesRead: Int = 15,
    val bookTitle: String = "Atomic Habits",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
