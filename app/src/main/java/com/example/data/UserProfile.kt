package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "", // Max 6-10 letters/numbers. If empty, displays "User"
    val name: String = "Morgan Reed",
    val email: String = "",
    val gender: String = "Reader",
    val age: Int = 27,
    val preferredStyles: String = "Science Fiction, Mystery, Literary Fiction",
    val bio: String = "Bibliophile exploring alternate worlds and classic mysteries. Tracking books, audiobooks & library checkouts.",
    val locationCity: String = "San Diego",
    val locationZip: String = "92101",
    val cardNumber: String = "LIB-7842-CA",
    val memberSince: String = "2024",
    val dailyReadingGoalMinutes: Int = 30,
    val followedAuthors: String = "Emily Henry, Andy Weir", // Comma-separated authors user follows
    val autoDeleteMessages: Boolean = false, // Setting in preferences to auto-delete in-app neighborhood messages
    val hasCompletedWelcome: Boolean = false,
    val isGuest: Boolean = false
) {
    // Label for navigation and headings: "User" until user specifies username
    val displayUserName: String
        get() = when {
            username.isNotBlank() -> username.take(10)
            isGuest -> "Guest"
            else -> "User"
        }

    fun getFollowedAuthorsList(): List<String> =
        followedAuthors.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    fun isFollowingAuthor(author: String): Boolean {
        if (author.isBlank()) return false
        val clean = author.trim().lowercase()
        return getFollowedAuthorsList().any { it.trim().lowercase() == clean }
    }
}
