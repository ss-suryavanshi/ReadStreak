package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["title"]),
        Index(value = ["dateAdded"])
    ]
)
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val author: String,
    val currentPage: Int = 0,
    val totalPages: Int = 300,
    val coverUrl: String = "",
    val isCompleted: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
)
