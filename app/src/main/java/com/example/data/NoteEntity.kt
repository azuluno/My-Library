package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val chapter: String = "",
    val pageNumber: Int? = null,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
