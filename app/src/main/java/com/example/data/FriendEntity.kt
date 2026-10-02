package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val displayName: String,
    val bio: String = "",
    val avatarUrl: String = "",
    val currentlyReadingTitle: String = "",
    val currentlyReadingAuthor: String = "",
    val currentlyReadingProgress: Int = 0, // 0 - 100
    val currentlyReadingChapter: String = "Chapter 1",
    val currentlyReadingCoverUrl: String = "",
    val booksFinishedCount: Int = 14,
    val isFriend: Boolean = true
)
