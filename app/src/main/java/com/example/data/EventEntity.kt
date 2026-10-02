package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val locationName: String,
    val address: String,
    val city: String,
    val zipCode: String,
    val date: String,
    val time: String,
    val category: String, // "Book Club", "Author Signing", "Library Workshop", "Storytime", "Book Sale"
    val description: String,
    val attendeeCount: Int = 18,
    val isSaved: Boolean = false
)
