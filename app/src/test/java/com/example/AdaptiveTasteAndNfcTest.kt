package com.example

import com.example.ai.AdaptiveTasteEngine
import com.example.ai.BookAnalysisResult
import com.example.data.*
import org.junit.Assert.*
import org.junit.Test

class AdaptiveTasteAndNfcTest {

    @Test
    fun testLearnedPreferencesShiftsToThrillersAndMysteries() {
        val profile = UserProfileEntity(
            preferredStyles = "Romance"
        )

        // User starts reading and finishing Thriller and Mystery books with high ratings
        val books = listOf(
            BookEntity(
                title = "The Silent Patient",
                author = "Alex Michaelides",
                genre = "Psychological Thriller",
                status = BookStatus.CURRENTLY_READING.name,
                userRating = 5.0,
                lastUpdated = 3000L
            ),
            BookEntity(
                title = "The Maid",
                author = "Nita Prose",
                genre = "Mystery",
                status = BookStatus.FINISHED.name,
                userRating = 4.8,
                lastUpdated = 2000L
            ),
            BookEntity(
                title = "Beach Read",
                author = "Emily Henry",
                genre = "Romance",
                status = BookStatus.FINISHED.name,
                userRating = 4.0,
                lastUpdated = 1000L
            )
        )

        val taste = AdaptiveTasteEngine.analyzeTaste(books, profile)

        // Top genres should now include Thriller and Mystery alongside Romance
        val topGenreNames = taste.topGenres.map { it.genre }
        assertTrue("Expected Thriller in top genres", topGenreNames.contains("Thriller"))
        assertTrue("Expected Mystery in top genres", topGenreNames.contains("Mystery"))
        assertTrue("Expected Romance in top genres", topGenreNames.contains("Romance"))

        // Thriller should have higher percentage/affinity due to CURRENTLY_READING + 5.0 user rating
        val thrillerAffinity = taste.topGenres.firstOrNull { it.genre == "Thriller" }?.affinityScore ?: 0f
        val romanceAffinity = taste.topGenres.firstOrNull { it.genre == "Romance" }?.affinityScore ?: 0f
        assertTrue("Thriller affinity ($thrillerAffinity) should be high", thrillerAffinity > 0f)
    }

    @Test
    fun testFollowedAuthorPopulatesUpInResults() {
        val profile = UserProfileEntity(
            followedAuthors = "Andy Weir"
        )
        val taste = AdaptiveTasteEngine.analyzeTaste(emptyList(), profile)
        assertTrue(taste.followedAuthors.contains("Andy Weir"))

        val catalog = listOf(
            BookAnalysisResult(title = "Random Novel", author = "John Doe", genre = "Sci-Fi", rating = 4.5),
            BookAnalysisResult(title = "Project Hail Mary", author = "Andy Weir", genre = "Sci-Fi", rating = 4.7)
        )

        val ranked = AdaptiveTasteEngine.rankResults(catalog, taste)
        assertEquals("Andy Weir book should rank first because author is followed", "Project Hail Mary", ranked.first().title)
    }

    @Test
    fun testNeighborhoodCheckoutDurationsAndOverdue() {
        val now = System.currentTimeMillis()
        val sevenDaysMillis = 7L * 24 * 60 * 60 * 1000

        val co = NeighborhoodCheckoutEntity(
            nfcTagId = "NFC-TEST-01",
            bookTitle = "Dune",
            bookAuthor = "Frank Herbert",
            personName = "Alex",
            role = CheckoutRole.LENT_TO_FRIEND.name,
            checkoutDateMillis = now,
            returnDurationDays = 7,
            dueDateMillis = now + sevenDaysMillis
        )

        assertEquals(7, co.returnDurationDays)
        assertFalse(co.isOverdue)
        assertTrue(co.daysRemaining in 6..7)

        val overdueCheckout = co.copy(
            dueDateMillis = now - (2L * 24 * 60 * 60 * 1000) // 2 days ago
        )
        assertTrue(overdueCheckout.isOverdue)
        assertTrue(overdueCheckout.daysRemaining < 0)
    }

    @Test
    fun testUserProfileFollowedAuthorHelpers() {
        val user = UserProfileEntity(
            followedAuthors = "Emily Henry, Andy Weir"
        )

        assertTrue(user.isFollowingAuthor("Emily Henry"))
        assertTrue(user.isFollowingAuthor("andy weir")) // Case insensitive
        assertFalse(user.isFollowingAuthor("Stephen King"))
    }
}
