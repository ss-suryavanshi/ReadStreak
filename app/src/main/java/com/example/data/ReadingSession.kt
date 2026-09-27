package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing an individual reading session logged by the user,
 * storing the book title, duration in minutes, pages read, date, and session notes.
 */
@Entity(
    tableName = "reading_sessions",
    indices = [
        Index(value = ["dateString"]),
        Index(value = ["bookTitle"]),
        Index(value = ["timestamp"])
    ]
)
data class ReadingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bookTitle: String,
    val minutesRead: Int,
    val pagesRead: Int,
    val dateString: String, // format "YYYY-MM-DD"
    val notes: String = "",
    val xpEarned: Int = 50,
    val timestamp: Long = System.currentTimeMillis()
)
