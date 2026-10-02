package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "", // Max 6 letters/numbers. If empty, displays "User"
    val name: String = "Morgan Reed",
    val email: String = "",
    val gender: String = "Reader",
    val age: Int = 27,
    val preferredStyles: String = "Science Fiction, Mystery, Literary Fiction",
    val bio: String = "Bibliophile exploring alternate worlds and classic mysteries. Currently tackling my 50-book annual reading goal.",
    val locationCity: String = "San Diego",
    val locationZip: String = "92101",
    val cardNumber: String = "LIB-7842-CA",
    val memberSince: String = "2024",
    val dailyReadingGoalMinutes: Int = 30
) {
    // Label for navigation and headings: "User" until user specifies username (max 6 chars)
    val displayUserName: String
        get() = if (username.isNotBlank()) username.take(6) else "User"
}
