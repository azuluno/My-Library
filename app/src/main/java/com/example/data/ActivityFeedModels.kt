package com.example.data

data class ActivityComment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val author: String,
    val text: String,
    val timeAgo: String = "Just now",
    val timestamp: Long = System.currentTimeMillis()
)

data class ActivityFeedItem(
    val id: String,
    val friendUsername: String,
    val friendName: String,
    val friendAvatar: String,
    val actionType: String, // "FINISHED", "READING", "WANT_TO_READ"
    val bookTitle: String,
    val bookAuthor: String,
    val bookCover: String,
    val rating: Double? = null,
    val progressPercent: Int? = null,
    val currentChapter: String? = null,
    val noteSnippet: String? = null,
    val timestamp: Long,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val comments: List<ActivityComment> = emptyList()
)
