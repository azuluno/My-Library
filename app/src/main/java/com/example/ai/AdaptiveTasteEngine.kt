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
    val declaredPreferences: List<String>,
    val followedAuthors: List<String> = emptyList()
)

object AdaptiveTasteEngine {

    /**
     * Dynamically learns the user's reading taste based on:
     * 1. Declared preferences in their Library Card / Profile
     * 2. Followed authors (chosen in preferences or heart-clicked on books)
     * 3. What they are currently reading, want to read, and finished
     * 4. How frequently and recently they read certain styles (e.g., shifting into Thrillers & Mysteries)
     * 5. Personal star ratings given to books (higher ratings boost genre affinity)
     */
    fun analyzeTaste(books: List<BookEntity>, profile: UserProfileEntity?): AdaptiveTasteProfile {
        val declared = profile?.preferredStyles
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()

        val followed = profile?.getFollowedAuthorsList().orEmpty()

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

            // Recency multiplier: the most recent 3 books have 1.5x influence
            if (index < 3) {
                weight *= 1.5f
            }

            // User personal star rating influence (1.0 to 5.0)
            if (book.userRating != null) {
                val ratingScore = book.userRating
                genreRatings.getOrPut(normGenre) { mutableListOf() }.add(ratingScore)
                // High rating boosts affinity heavily; low rating reduces it
                weight *= when {
                    ratingScore >= 4.5 -> 1.8f
                    ratingScore >= 4.0 -> 1.4f
                    ratingScore >= 3.0 -> 1.0f
                    else -> 0.5f // Disliked book dampens genre preference
                }
            }

            // Followed author bonus: if book is by a followed author, gives additional weight
            if (followed.any { book.author.contains(it, ignoreCase = true) }) {
                weight *= 1.4f
            }

            genreScores[normGenre] = (genreScores[normGenre] ?: 0f) + weight
        }

        // If user has zero books and zero declared, fallback to standard defaults
        if (genreScores.isEmpty()) {
            genreScores["Mystery & Thriller"] = 25f
            genreScores["Fiction"] = 20f
        }

        val totalScore = genreScores.values.sum().coerceAtLeast(1f)

        val sortedGenres = genreScores.entries
            .sortedByDescending { it.value }
            .take(6)
            .map { (genre, score) ->
                val ratings = genreRatings[genre]
                val avgRating = if (!ratings.isNullOrEmpty()) ratings.average() else null
                val pct = ((score / totalScore) * 100).toInt().coerceIn(1, 100)
                val isRecent = genreRecent.contains(genre)
                val isDeclared = declared.any { normalizeGenre(it).equals(genre, ignoreCase = true) }

                LearnedGenreAffinity(
                    genre = genre,
                    affinityScore = score,
                    percentage = pct,
                    bookCount = genreBookCounts[genre] ?: 0,
                    avgUserRating = avgRating,
                    isFromRecentReads = isRecent,
                    isFromLibraryCard = isDeclared
                )
            }

        // Detect primary reading focus and evolving summary
        val top1 = sortedGenres.firstOrNull()?.genre ?: "Diverse Fiction"
        val top2 = sortedGenres.getOrNull(1)?.genre

        val primaryFocus = if (top2 != null) "$top1 & $top2" else top1

        val hasRecentShift = sortedGenres.any { it.isFromRecentReads && !it.isFromLibraryCard }
        val evolvingSummary = when {
            hasRecentShift -> "Your reading patterns have shifted towards $top1 based on your recent books & ratings."
            sortedGenres.isNotEmpty() -> "Reflecting your favorite categories from your library card and your top-rated reads."
            else -> "Personalizing based on your reading progress and bookshelf additions."
        }

        val ratedBooks = books.filter { it.userRating != null }
        val overallAvgRating = if (ratedBooks.isNotEmpty()) {
            ratedBooks.mapNotNull { it.userRating }.average()
        } else 0.0

        return AdaptiveTasteProfile(
            topGenres = sortedGenres,
            primaryReadingFocus = primaryFocus,
            evolvingSummary = evolvingSummary,
            ratedBooksCount = ratedBooks.size,
            avgUserRating = overallAvgRating,
            declaredPreferences = declared,
            followedAuthors = followed
        )
    }

    /**
     * Clean/normalize genre strings
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
     * Ranks books or search results based on user's learned taste and followed authors.
     * Followed authors and high affinity genres appear FIRST.
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

            // Bonus for followed authors (+50 points so they populate up higher!)
            if (profile.followedAuthors.any { author -> book.author.contains(author, ignoreCase = true) }) {
                score += 50f
            }

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

    /**
     * Ranks books in the user's library when opening the app.
     * Followed authors populate up first (+100 score), followed by current reading & top genre affinity.
     */
    fun rankLibraryBooks(
        books: List<BookEntity>,
        profile: AdaptiveTasteProfile?
    ): List<BookEntity> {
        if (profile == null) return books

        val followed = profile.followedAuthors
        val genreMap = profile.topGenres.associate { normalizeGenre(it.genre).lowercase() to it.affinityScore }

        return books.sortedWith(
            compareByDescending<BookEntity> { book ->
                // Priority 1: Followed author (+100)
                if (followed.any { book.author.contains(it, ignoreCase = true) }) 100f else 0f
            }.thenByDescending { book ->
                // Priority 2: Status (Currently reading first)
                if (book.status == BookStatus.CURRENTLY_READING.name) 50f else 0f
            }.thenByDescending { book ->
                // Priority 3: Genre Affinity Score
                genreMap[normalizeGenre(book.genre).lowercase()] ?: 0f
            }.thenByDescending { book ->
                // Priority 4: User personal rating or general rating
                (book.userRating ?: book.rating).toFloat()
            }.thenByDescending { book ->
                book.lastUpdated
            }
        )
    }
}
