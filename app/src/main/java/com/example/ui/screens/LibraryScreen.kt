package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ai.AdaptiveTasteEngine
import com.example.ai.AdaptiveTasteProfile
import com.example.ai.BookAnalysisResult
import com.example.data.BookEntity
import com.example.data.BookStatus
import com.example.data.NeighborhoodCheckoutEntity
import com.example.data.UserProfileEntity
import com.example.ui.components.FilterSortState
import com.example.ui.components.SortDirection
import com.example.ui.components.SortField
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LibraryScreen(
    books: List<BookEntity>,
    userProfile: UserProfileEntity?,
    currentFilter: String,
    filterSortState: FilterSortState,
    showCurrentlyReadingWelcome: Boolean,
    adaptiveTasteProfile: AdaptiveTasteProfile? = null,
    topRatedBooks: List<BookEntity> = emptyList(),
    adaptiveRecommendations: List<BookAnalysisResult> = emptyList(),
    isLoadingRecommendations: Boolean = false,
    checkouts: List<NeighborhoodCheckoutEntity> = emptyList(),
    pendingAlertsCount: Int = 0,
    onFilterChange: (String) -> Unit,
    onSelectBook: (BookEntity) -> Unit,
    onOpenLibraryCard: () -> Unit,
    onOpenFilterSort: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenNotifications: () -> Unit = {},
    onOpenNfcCheckout: (BookEntity?) -> Unit = {},
    onOpenMessageForCheckout: (NeighborhoodCheckoutEntity) -> Unit = {},
    onMarkCheckoutReturned: (Long) -> Unit = {},
    onToggleFollowAuthor: (String) -> Unit = {},
    onAddNewBook: () -> Unit,
    onRefreshRecommendations: () -> Unit = {},
    onAddRecommendationToLibrary: (BookAnalysisResult) -> Unit = {}
) {
    val currentlyReading = books.filter { it.status == BookStatus.CURRENTLY_READING.name }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    val filteredBooks = remember(books, currentFilter, filterSortState, adaptiveTasteProfile, userProfile) {
        val list = books.filter { book ->
            val matchesTab = when (currentFilter) {
                "ALL" -> true
                "AI_DISCOVER" -> false // Rendered separately as dedicated tab
                "NEIGHBORHOOD" -> false // Rendered separately as dedicated tab
                "RATED" -> book.userRating != null
                "BOOKS" -> !book.isAudiobook
                "AUDIOBOOKS" -> book.isAudiobook
                "CURRENT" -> book.status == BookStatus.CURRENTLY_READING.name
                "WANT" -> book.status == BookStatus.WANT_TO_READ.name
                "FINISHED" -> book.status == BookStatus.FINISHED.name
                "CHECKED_OUT" -> book.isCheckedOut
                else -> true
            }

            val matchesGenre = filterSortState.selectedGenre == null ||
                    book.genre.contains(filterSortState.selectedGenre, ignoreCase = true)
            val matchesAuthor = filterSortState.selectedAuthor == null ||
                    book.author.equals(filterSortState.selectedAuthor, ignoreCase = true)
            val matchesStatus = filterSortState.selectedStatus == null ||
                    book.status == filterSortState.selectedStatus
            val matchesRating = filterSortState.minRating == null ||
                    book.rating >= filterSortState.minRating
            val matchesEra = when (filterSortState.selectedPublicationEra) {
                "Classic (Pre-2000)" -> book.publicationYear < 2000
                "Modern (2000-2020)" -> book.publicationYear in 2000..2020
                "Recent (2021+)" -> book.publicationYear >= 2021
                else -> true
            }

            matchesTab && matchesGenre && matchesAuthor && matchesStatus && matchesRating && matchesEra
        }

        if (currentFilter == "RATED") {
            // All titles and authors user rated, ranging from top rated to least
            list.sortedWith(
                compareByDescending<BookEntity> { it.userRating ?: 0.0 }
                    .thenByDescending { it.rating }
                    .thenBy { it.title }
            )
        } else if (currentFilter == "ALL" && filterSortState.selectedGenre == null && filterSortState.selectedAuthor == null && filterSortState.selectedStatus == null) {
            // When opening the app on ALL, followed authors populate up first!
            AdaptiveTasteEngine.rankLibraryBooks(list, adaptiveTasteProfile)
        } else {
            when (filterSortState.sortField) {
                SortField.TITLE -> if (filterSortState.sortDirection == SortDirection.ASCENDING) list.sortedBy { it.title.lowercase() } else list.sortedByDescending { it.title.lowercase() }
                SortField.AUTHOR -> if (filterSortState.sortDirection == SortDirection.ASCENDING) list.sortedBy { it.author.lowercase() } else list.sortedByDescending { it.author.lowercase() }
                SortField.RATING -> if (filterSortState.sortDirection == SortDirection.ASCENDING) list.sortedBy { it.rating } else list.sortedByDescending { it.rating }
                SortField.DATE_ADDED -> if (filterSortState.sortDirection == SortDirection.ASCENDING) list.sortedBy { it.dateAdded } else list.sortedByDescending { it.dateAdded }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNewBook,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Book") },
                containerColor = LibraryForestGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_book")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("library_screen"),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Header Bar: Title & Spaced-Out Rounded Action Icons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Library",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LibraryForestGreen
                    )

                    // Spaced out top icons with fully rounded backgrounds that never touch
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Notifications / Alerts Bell Button (Fully rounded CircleShape)
                        Surface(
                            shape = CircleShape,
                            color = if (pendingAlertsCount > 0) Color(0xFFFFF3CD) else Color(0xFFF4F6F0),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (pendingAlertsCount > 0) Color(0xFFFFB300) else Color(0xFFCFE1D4)
                            ),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onOpenNotifications() }
                                    .testTag("open_notifications_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (pendingAlertsCount > 0) {
                                            Badge(
                                                containerColor = Color(0xFFD32F2F),
                                                contentColor = Color.White
                                            ) {
                                                Text(pendingAlertsCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (pendingAlertsCount > 0) Icons.Default.NotificationsActive else Icons.Outlined.Notifications,
                                        contentDescription = "Notifications & Alerts",
                                        tint = if (pendingAlertsCount > 0) Color(0xFFC62828) else LibraryForestGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // 2. NFC Loan Button (Fully rounded CircleShape)
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF4F6F0),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFCFE1D4)),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onOpenNfcCheckout(null) }
                                    .testTag("header_nfc_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = "NFC Neighborhood Checkout",
                                    tint = LibraryForestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // 3. Analytics Button (Fully rounded CircleShape)
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF4F6F0),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFCFE1D4)),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onOpenAnalytics() }
                                    .testTag("open_analytics_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Reading Progress Analytics",
                                    tint = LibraryForestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // 4. Library Card Button (Fully rounded CircleShape)
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, LibraryForestGreen.copy(alpha = 0.5f)),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onOpenLibraryCard() }
                                    .testTag("user_library_card_badge")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = "View Library Card",
                                    tint = LibraryForestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Prominent Text Under Header (Large, Bold, High-Contrast & Highly Readable)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    AnimatedContent(
                        targetState = showCurrentlyReadingWelcome && currentlyReading.isNotEmpty(),
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "welcome_title_anim"
                    ) { isReadingMode ->
                        if (isReadingMode) {
                            val active = currentlyReading.first()
                            Column(
                                modifier = Modifier.clickable { onSelectBook(active) }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = LibraryAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Currently Reading",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF14241B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${active.title} • ${active.currentChapter}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Column {
                                Text(
                                    text = "Welcome back, ${userProfile?.displayUserName ?: "Reader"}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF14241B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${books.size} Books on Shelves • Patron Card #${userProfile?.cardNumber ?: "LIB-7842-SD"}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen
                                )
                            }
                        }
                    }
                }
            }

            // Currently Reading Quick Progress Card (if any)
            if (currentlyReading.isNotEmpty()) {
                item {
                    val active = currentlyReading.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectBook(active) }
                            .testTag("currently_reading_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F7F1)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LibraryGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(width = 46.dp, height = 66.dp),
                                shadowElevation = 2.dp
                            ) {
                                if (active.coverUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = active.coverUrl,
                                        contentDescription = active.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(LibraryForestGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (active.isAudiobook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = LibraryGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = active.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = active.currentChapter,
                                    fontSize = 11.sp,
                                    color = LibraryAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (active.isAudiobook) "${active.listenedMinutes}/${active.totalDurationMinutes} mins" else "Page ${active.currentPage} of ${active.totalPages}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { active.progressPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = LibraryAmber,
                                    trackColor = Color(0xFFEDE8DD)
                                )
                            }
                        }
                    }
                }
            }

            // Horizontally Scrollable Middle Filter Bar (Front & Center in the middle!)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHELVES & FILTER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LibraryForestGreen,
                        letterSpacing = 1.sp
                    )

                    // Advanced Filter & Sort trigger
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (filterSortState.activeFilterCount > 0) LibraryGold.copy(alpha = 0.2f) else Color(0xFFECE6DC))
                            .clickable { onOpenFilterSort() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Advanced Filter & Sort",
                            tint = LibraryForestGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (filterSortState.activeFilterCount > 0) "Filter (${filterSortState.activeFilterCount})" else "Sort & Filter",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LibraryForestGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val ratedCount = books.count { it.userRating != null }
                val readingCount = books.count { it.status == BookStatus.CURRENTLY_READING.name }
                val wantCount = books.count { it.status == BookStatus.WANT_TO_READ.name }
                val finishedCount = books.count { it.status == BookStatus.FINISHED.name }
                val audioCount = books.count { it.isAudiobook }
                val checkedOutCount = books.count { it.isCheckedOut }
                val activeNeighborhoodCount = checkouts.count { !it.isReturned }

                val filters = listOf(
                    "ALL" to "All (${books.size})",
                    "AI_DISCOVER" to "✨ AI Discover",
                    "CURRENT" to "Reading ($readingCount)",
                    "WANT" to "Want to Read ($wantCount)",
                    "FINISHED" to "Finished ($finishedCount)",
                    "RATED" to "Top Rated ★ ($ratedCount)",
                    "NEIGHBORHOOD" to "Neighborhood 🤝 ($activeNeighborhoodCount)",
                    "AUDIOBOOKS" to "Audiobooks 🎧 ($audioCount)",
                    "CHECKED_OUT" to "Checked Out 🏷 ($checkedOutCount)"
                )

                // Smoothly horizontally scrolling TabRow so tabs never overlap screen borders
                ScrollableTabRow(
                    selectedTabIndex = filters.indexOfFirst { it.first == currentFilter }.coerceAtLeast(0),
                    edgePadding = 20.dp,
                    containerColor = Color.Transparent,
                    contentColor = LibraryForestGreen,
                    indicator = {},
                    divider = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scrollable_filter_bar")
                ) {
                    filters.forEach { (key, label) ->
                        val selected = currentFilter == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) LibraryForestGreen else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) LibraryForestGreen else Color(0xFFDED8CD)),
                            shadowElevation = if (selected) 2.dp else 0.dp,
                            modifier = Modifier
                                .padding(end = 8.dp, bottom = 4.dp)
                                .clickable { onFilterChange(key) }
                                .testTag("tab_filter_$key")
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // TAB VIEW 1: DEDICATED AI DISCOVERY TAB
            // (Neatly in its own tab, NOT covering half the page on ALL!)
            // ==========================================
            if (currentFilter == "AI_DISCOVER") {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = LibraryAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GEMINI ADAPTIVE TASTE & PICKS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen,
                                    letterSpacing = 1.sp
                                )
                            }

                            IconButton(
                                onClick = onRefreshRecommendations,
                                modifier = Modifier.size(28.dp)
                            ) {
                                if (isLoadingRecommendations) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = LibraryForestGreen
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = LibraryForestGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE4DFD5)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Learned Taste: ${adaptiveTasteProfile?.primaryReadingFocus ?: "Eclectic Fiction"}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = adaptiveTasteProfile?.evolvingSummary ?: "Adapting dynamically to your reading frequency, ratings, and followed authors.",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Dynamic Genre Affinity Badges
                                val topG = adaptiveTasteProfile?.topGenres.orEmpty().take(5)
                                if (topG.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        topG.forEach { item ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (item.isFromRecentReads) LibraryAmber.copy(alpha = 0.15f) else Color(0xFFEFECE4)
                                            ) {
                                                Text(
                                                    text = "${item.genre} (${item.percentage}%)",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (item.isFromRecentReads) LibraryAmber else TextPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (adaptiveRecommendations.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "RECOMMENDED FOR YOUR TASTE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(adaptiveRecommendations) { rec ->
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF7F2)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFECE7DE)),
                                                modifier = Modifier.width(170.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(100.dp),
                                                        shadowElevation = 2.dp
                                                    ) {
                                                        AsyncImage(
                                                            model = rec.coverUrl,
                                                            contentDescription = rec.title,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Text(
                                                        text = rec.title,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = rec.author,
                                                        fontSize = 11.sp,
                                                        color = TextSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = LibraryGold.copy(alpha = 0.2f)
                                                        ) {
                                                            Text(
                                                                text = "★ ${"%.1f".format(rec.rating)}",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF8B6420),
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                            )
                                                        }

                                                        FilledTonalButton(
                                                            onClick = { onAddRecommendationToLibrary(rec) },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                                containerColor = LibraryForestGreen,
                                                                contentColor = Color.White
                                                            ),
                                                            modifier = Modifier.height(26.dp)
                                                        ) {
                                                            Text("+ Add", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // TAB VIEW 2: DEDICATED NEIGHBORHOOD LENDING & NFC TAB
            // ==========================================
            if (currentFilter == "NEIGHBORHOOD") {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NEIGHBORHOOD LENDING & CHECKOUT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LibraryForestGreen,
                                letterSpacing = 1.sp
                            )

                            FilledTonalButton(
                                onClick = { onOpenNfcCheckout(null) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = LibraryForestGreen,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Nfc, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ New Loan", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (checkouts.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFECE7DE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sensors,
                                        contentDescription = null,
                                        tint = LibraryAmber,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No Neighborhood Checkouts Yet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = LibraryForestGreen
                                    )
                                    Text(
                                        text = "Attach an NFC tag to any book to lend to friends or track borrowed books!",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                items(checkouts) { co ->
                    val isOverdue = co.isOverdue
                    val isDueSoon = co.isDueSoon
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                co.isReturned -> Color(0xFFF7F5F0)
                                isOverdue -> Color(0xFFFFF0F0)
                                isDueSoon -> Color(0xFFFFF9E6)
                                else -> Color.White
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5DFD4)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Nfc,
                                        contentDescription = null,
                                        tint = LibraryForestGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${if (co.isLentToFriend) "🤝 Lent to" else "📖 Borrowed from"} ${co.personName}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LibraryForestGreen
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (co.isReturned) Color(0xFFE8F5E9) else if (isOverdue) Color(0xFFFFCDD2) else Color(0xFFFFF3CD)
                                ) {
                                    Text(
                                        text = when {
                                            co.isReturned -> "RETURNED"
                                            isOverdue -> "OVERDUE"
                                            else -> "DUE IN ${co.daysRemaining} DAYS"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (co.isReturned) Color(0xFF2E7D32) else if (isOverdue) Color(0xFFC62828) else Color(0xFF8B6420),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = co.bookTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "by ${co.bookAuthor} • Tag: ${co.nfcTagId}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Return By: ${dateFormat.format(Date(co.dueDateMillis))}",
                                fontSize = 11.sp,
                                color = Color(0xFF6B4500)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onOpenMessageForCheckout(co) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Message", fontSize = 11.sp)
                                }

                                if (!co.isReturned) {
                                    Button(
                                        onClick = { onMarkCheckoutReturned(co.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LibraryForestGreen),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Mark Returned", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Top Rated Banner when viewing RATED filter
            if (currentFilter == "RATED") {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LibraryGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = LibraryGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Your Top Rated Books (${filteredBooks.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5D4037)
                                )
                                Text(
                                    text = "All books you've rated, ranked from highest star rating to least.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF795548)
                                )
                            }
                        }
                    }
                }
            }

            // Empty State
            if (filteredBooks.isEmpty() && currentFilter != "AI_DISCOVER" && currentFilter != "NEIGHBORHOOD") {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFFC0B8AC),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (currentFilter == "RATED") "No books rated yet" else "No matching titles found",
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentFilter == "RATED") "Give star ratings to books in your library to build your top rated list!" else "Try adjusting your shelf or filter settings.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else if (currentFilter != "AI_DISCOVER" && currentFilter != "NEIGHBORHOOD") {
                itemsIndexed(filteredBooks, key = { _, book -> book.id }) { index, book ->
                    val isFollowed = userProfile?.isFollowingAuthor(book.author) == true
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectBook(book) }
                            .testTag("book_item_${book.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDE8DD))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(width = 56.dp, height = 80.dp),
                                    shadowElevation = 3.dp
                                ) {
                                    if (book.coverUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = book.coverUrl,
                                            contentDescription = book.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(LibraryForestGreen),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (book.isAudiobook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                                                contentDescription = null,
                                                tint = LibraryGold,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }

                                if (currentFilter == "RATED") {
                                    Surface(
                                        shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                                        color = if (index == 0) LibraryGold else LibraryForestGreen,
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (index == 0) Color(0xFF3E2723) else Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = book.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (book.isCheckedOut) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFE3F2FD)
                                        ) {
                                            Text(
                                                text = if (book.neighborhoodBorrower != null) "🤝 Lent" else "Checked Out",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1976D2),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // Author row with little heart to follow/unfollow author!
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "by ${book.author}",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onToggleFollowAuthor(book.author) },
                                        modifier = Modifier.size(22.dp).testTag("author_heart_${book.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isFollowed) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = if (isFollowed) "Unfollow ${book.author}" else "Follow ${book.author}",
                                            tint = if (isFollowed) Color(0xFFE91E63) else Color.Gray,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    if (isFollowed) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFFEBEE)
                                        ) {
                                            Text(
                                                text = "♥ Followed",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC2185B),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Personal user rating if rated
                                    if (book.userRating != null) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFFF3CD),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, LibraryGold.copy(alpha = 0.6f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = LibraryGold,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "${"%.1f".format(book.userRating)} ★ You",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF6B4500)
                                                )
                                            }
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFE5A93C),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "${book.rating} (${book.ratingSource})",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Text(
                                        text = "• ${book.genre}",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
