package com.example.ai

import com.example.data.BookEntity
import com.example.data.BookStatus
import com.example.data.UserProfileEntity

data class LearnedGenreAffinity(
    val genre: String,
    val affinityScore: Float,
    val percentage: Int,
    val bookCount: Int,
    val avgUserRating: Double?,
    val isFromRecentReads: Boolean,
    val isFromLibraryCard: Boolean
)

data class AdaptiveTasteProfile(
    val topGenres: List<LearnedGenreAffinity>,
    val primaryReadingFocus: String,
    val evolvingSummary: String,
    val ratedBooksCount: Int,
    val avgUserRating: Double,
    val declaredPreferences: List<String>
)

object AdaptiveTasteEngine {

    /**
     * Dynamically learns the user's reading taste based on:
     * 1. Declared preferences in their Library Card / Profile
     * 2. What they are currently reading, want to read, and finished
     * 3. How frequently and recently they read certain styles (e.g., shifting into Thrillers & Mysteries)
     * 4. Personal star ratings given to books (higher ratings boost genre affinity)
     */
    fun analyzeTaste(books: List<BookEntity>, profile: UserProfileEntity?): AdaptiveTasteProfile {
        val declared = profile?.preferredStyles
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()

        val genreScores = mutableMapOf<String, Float>()
        val genreBookCounts = mutableMapOf<String, Int>()
        val genreRatings = mutableMapOf<String, MutableList<Double>>()
        val genreRecent = mutableSetOf<String>()

        // 1. Base weights from declared library card preferences
        for (style in declared) {
            val normalized = normalizeGenre(style)
            genreScores[normalized] = (genreScores[normalized] ?: 0f) + 12f
        }

        // 2. Behavioral weights from user's books & reading activities
        val sortedByRecent = books.sortedByDescending { it.lastUpdated }
        val recentCutoffIndex = (books.size / 2).coerceAtLeast(3)

        sortedByRecent.forEachIndexed { index, book ->
            val normGenre = normalizeGenre(book.genre)
            genreBookCounts[normGenre] = (genreBookCounts[normGenre] ?: 0) + 1

            if (index < recentCutoffIndex) {
                genreRecent.add(normGenre)
            }

            var weight = when (book.status) {
                BookStatus.CURRENTLY_READING.name -> 15f // Currently active books carry high weight
                BookStatus.FINISHED.name -> 10f
                BookStatus.WANT_TO_READ.name -> 6f
                else -> 5f
            }

            // Recency boost
            if (index < recentCutoffIndex) {
                weight *= 1.35f
            }

            // 3. User rating influence
            if (book.userRating != null) {
                genreRatings.getOrPut(normGenre) { mutableListOf() }.add(book.userRating)
                when {
                    book.userRating >= 4.5 -> weight += 18f // Major positive boost
                    book.userRating >= 4.0 -> weight += 10f
                    book.userRating >= 3.0 -> weight += 4f
                    else -> weight -= 5f // Low rating lowers affinity
                }
            }

            genreScores[normGenre] = (genreScores[normGenre] ?: 0f) + weight
        }

        val totalScore = genreScores.values.sum().coerceAtLeast(1f)
        val sortedGenres = genreScores.entries
            .sortedByDescending { it.value }
            .map { (genre, score) ->
                val count = genreBookCounts[genre] ?: 0
                val ratings = genreRatings[genre].orEmpty()
                val avgRating = if (ratings.isNotEmpty()) ratings.average() else null
                val pct = ((score / totalScore) * 100).toInt().coerceIn(1, 100)
                val isDeclared = declared.any { normalizeGenre(it) == genre }
                val isRecent = genreRecent.contains(genre)

                LearnedGenreAffinity(
                    genre = genre,
                    affinityScore = score,
                    percentage = pct,
                    bookCount = count,
                    avgUserRating = avgRating,
                    isFromRecentReads = isRecent,
                    isFromLibraryCard = isDeclared
                )
            }

        val ratedBooks = books.filter { it.userRating != null }
        val overallAvgRating = if (ratedBooks.isNotEmpty()) {
            ratedBooks.mapNotNull { it.userRating }.average()
        } else 0.0

        val topNames = sortedGenres.take(3).map { it.genre }
        val primaryFocus = when {
            topNames.size >= 2 -> "${topNames[0]} & ${topNames[1]}"
            topNames.isNotEmpty() -> topNames[0]
            else -> "Eclectic Fiction"
        }

        val evolvingSummary = buildString {
            if (topNames.isNotEmpty()) {
                append("Primary affinity in ${topNames.joinToString(", ")}. ")
            }
            if (genreRecent.isNotEmpty()) {
                val recents = genreRecent.take(2).joinToString(" & ")
                append("Recently reading more $recents. ")
            }
            if (declared.isNotEmpty()) {
                append("Library card preferences: ${declared.joinToString(", ")}. ")
            }
            if (ratedBooks.isNotEmpty()) {
                append("${ratedBooks.size} books rated (avg ${"%.1f".format(overallAvgRating)}★).")
            }
        }

        return AdaptiveTasteProfile(
            topGenres = sortedGenres,
            primaryReadingFocus = primaryFocus,
            evolvingSummary = evolvingSummary,
            ratedBooksCount = ratedBooks.size,
            avgUserRating = overallAvgRating,
            declaredPreferences = declared
        )
    }

    /**
     * Clean/normalize genre strings (e.g. "Psychological Thriller / Suspense" -> "Thriller")
     */
    fun normalizeGenre(raw: String): String {
        val clean = raw.trim()
        val lower = clean.lowercase()
        return when {
            lower.contains("thriller") -> "Thriller"
            lower.contains("mystery") -> "Mystery"
            lower.contains("romance") -> "Romance"
            lower.contains("sci-fi") || lower.contains("science fiction") -> "Science Fiction"
            lower.contains("fantasy") -> "Fantasy"
            lower.contains("historical") -> "Historical Fiction"
            lower.contains("literary") -> "Literary Fiction"
            lower.contains("memoir") || lower.contains("biography") -> "Memoir & Biography"
            lower.contains("self") || lower.contains("habit") -> "Personal Growth"
            lower.contains("magical") -> "Magical Realism"
            else -> clean.split("/").first().trim().replaceFirstChar { it.uppercase() }
        }
    }

    /**
     * Ranks books or search results based on user's learned taste.
     * Genres with high affinity appear FIRST.
     */
    fun rankResults(
        items: List<BookAnalysisResult>,
        profile: AdaptiveTasteProfile
    ): List<BookAnalysisResult> {
        val genreRankMap = profile.topGenres.mapIndexed { idx, item ->
            normalizeGenre(item.genre).lowercase() to (profile.topGenres.size - idx) * 10f
        }.toMap()

        return items.sortedByDescending { book ->
            val norm = normalizeGenre(book.genre).lowercase()
            var score = genreRankMap[norm] ?: 0f

            // Also check partial matches
            for ((g, bonus) in genreRankMap) {
                if (norm.contains(g) || book.synopsis.lowercase().contains(g)) {
                    score += bonus * 0.7f
                }
            }

            // Factor online rating as secondary tie-breaker
            score += (book.rating.toFloat() * 1.5f)
            score
        }
    }
}
