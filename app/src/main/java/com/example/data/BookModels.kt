package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemType {
    BOOK,
    AUDIOBOOK
}

enum class BookStatus {
    CURRENTLY_READING,
    WANT_TO_READ,
    FINISHED;

    fun getDisplayName(type: ItemType): String {
        return when (this) {
            CURRENTLY_READING -> if (type == ItemType.AUDIOBOOK) "Listening" else "Currently Reading"
            WANT_TO_READ -> if (type == ItemType.AUDIOBOOK) "Want to Listen" else "Want to Read"
            FINISHED -> "Finished"
        }
    }
}

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String = ItemType.BOOK.name,
    val title: String,
    val author: String,
    val synopsis: String = "",
    val coverUrl: String = "",
    val status: String = BookStatus.CURRENTLY_READING.name,
    val rating: Double = 4.5,
    val ratingSource: String = "Goodreads",
    val ratingCount: Int = 12000,
    val userRating: Double? = null, // Personal star rating given by the user (1.0 to 5.0)
    val totalPages: Int = 350,
    val currentPage: Int = 0,
    val currentChapter: String = "Chapter 1",
    val totalDurationMinutes: Int = 480, // 8 hours for audiobooks
    val listenedMinutes: Int = 0,
    val readingTimeMinutes: Int = 0, // Total time user spent reading/listening
    val isCheckedOut: Boolean = false,
    val checkoutLibrary: String? = null,
    val dueDate: String? = null,
    val genre: String = "General Fiction",
    val publicationYear: Int = 2022,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isAudiobook: Boolean
        get() = type == ItemType.AUDIOBOOK.name

    val progressPercent: Int
        get() {
            return if (isAudiobook) {
                if (totalDurationMinutes > 0) ((listenedMinutes.toFloat() / totalDurationMinutes) * 100).toInt().coerceIn(0, 100) else 0
            } else {
                if (totalPages > 0) ((currentPage.toFloat() / totalPages) * 100).toInt().coerceIn(0, 100) else 0
            }
        }
}
