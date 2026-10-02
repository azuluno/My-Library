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
import com.example.ai.AdaptiveTasteProfile
import com.example.ai.BookAnalysisResult
import com.example.data.BookEntity
import com.example.data.BookStatus
import com.example.data.ItemType
import com.example.data.UserProfileEntity
import com.example.ui.components.FilterSortState
import com.example.ui.components.SortDirection
import com.example.ui.components.SortField
import com.example.ui.theme.*

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
    onFilterChange: (String) -> Unit,
    onSelectBook: (BookEntity) -> Unit,
    onOpenLibraryCard: () -> Unit,
    onOpenFilterSort: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onAddNewBook: () -> Unit,
    onRefreshRecommendations: () -> Unit = {},
    onAddRecommendationToLibrary: (BookAnalysisResult) -> Unit = {}
) {
    val currentlyReading = books.filter { it.status == BookStatus.CURRENTLY_READING.name }

    val filteredBooks = remember(books, currentFilter, filterSortState) {
        var list = books.filter { book ->
            val matchesTab = when (currentFilter) {
                "ALL" -> true
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
            // "rated book section there should be a section where all the titles and authors of the books
            // they have already rated will show in the top rated list, ranging from top rated to least"
            list.sortedWith(
                compareByDescending<BookEntity> { it.userRating ?: 0.0 }
                    .thenByDescending { it.rating }
                    .thenBy { it.title }
            )
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
            // Header Bar with Brighter Green & 30s Transition Welcome Message
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LibraryForestGreen
                                        )
                                    }
                                    Text(
                                        text = "${active.title} • ${active.currentChapter}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Column {
                                    Text(
                                        text = "My Library",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LibraryForestGreen
                                    )
                                    Text(
                                        text = "Welcome back, ${userProfile?.displayUserName ?: "User"}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Analytics Button
                        IconButton(
                            onClick = onOpenAnalytics,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE8F5E9))
                                .testTag("open_analytics_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Reading Progress Analytics",
                                tint = LibraryForestGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Library Card Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFD4E8DC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LibraryForestGreen.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .clickable { onOpenLibraryCard() }
                                .testTag("user_library_card_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = "View Library Card",
                                    tint = LibraryForestGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Card",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen
                                )
                            }
                        }
                    }
                }
            }

            // Hero banner with decorative visual asset
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onOpenLibraryCard() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(115.dp)) {
                        AsyncImage(
                            model = "android.resource://com.aistudio.mylibrary.bkqpxz/drawable/img_library_hero_1790902920250",
                            error = painterResource(R.drawable.ic_app_logo_1790902863935),
                            contentDescription = "Library Nook",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(
                                            LibraryForestGreen.copy(alpha = 0.88f),
                                            LibraryForestGreen.copy(alpha = 0.40f)
                                        )
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text(
                                    text = "Your Reading Sanctuary",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${books.size} Titles • ${currentlyReading.size} Reading Now • Tap for Card",
                                    color = LibraryGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Where You Left Off Spotlight
            if (currentlyReading.isNotEmpty()) {
                item {
                    val active = currentlyReading.first()
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = LibraryAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WHERE YOU LEFT OFF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LibraryForestGreen,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectBook(active) }
                                .testTag("where_you_left_off_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5DFD4))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(width = 54.dp, height = 76.dp),
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
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = active.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = active.currentChapter,
                                        fontSize = 12.sp,
                                        color = LibraryAmber,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (active.isAudiobook) "${active.listenedMinutes}/${active.totalDurationMinutes} mins listened" else "Page ${active.currentPage} of ${active.totalPages}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { active.progressPercent / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = LibraryAmber,
                                        trackColor = Color(0xFFEDE8DD)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Gemini Adaptive Taste & Discovery Shelf (Adapts dynamically to user's reading trends and ratings!)
            if (currentFilter == "ALL") {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
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
                                    text = "GEMINI ADAPTIVE DISCOVERY",
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
                                        contentDescription = "Refresh Recommendations",
                                        tint = LibraryForestGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE4DFD5)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "AI Learned Taste: ${adaptiveTasteProfile?.primaryReadingFocus ?: "Eclectic Fiction"}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Adapts as you add books, read genres (like Thrillers & Mysteries), and give ratings.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Dynamic Genre Affinity Badges
                                val topG = adaptiveTasteProfile?.topGenres.orEmpty().take(4)
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
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (adaptiveRecommendations.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
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

                                                    Spacer(modifier = Modifier.height(4.dp))

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

            // Dedicated Top Rated Books Section (Titles and Authors Ranked From Top Rated to Least!)
            if (currentFilter == "ALL" && topRatedBooks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = LibraryGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "YOUR TOP RATED BOOKS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen,
                                    letterSpacing = 1.sp
                                )
                            }

                            Text(
                                text = "View All (${topRatedBooks.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LibraryAmber,
                                modifier = Modifier
                                    .clickable { onFilterChange("RATED") }
                                    .testTag("view_all_rated_button")
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(topRatedBooks) { index, book ->
                                Card(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onSelectBook(book) }
                                        .testTag("top_rated_card_$index"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5DFD4))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Box {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(110.dp),
                                                shadowElevation = 2.dp
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
                                                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                                            contentDescription = null,
                                                            tint = LibraryGold,
                                                            modifier = Modifier.size(28.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Rank Badge (#1, #2, #3...)
                                            Surface(
                                                shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                                                color = if (index == 0) LibraryGold else LibraryForestGreen,
                                                modifier = Modifier.align(Alignment.TopStart)
                                            ) {
                                                Text(
                                                    text = "#${index + 1}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (index == 0) Color(0xFF3E2723) else Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = book.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "by ${book.author}",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFFF7E6),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE0A3))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFE5A93C),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "${book.userRating ?: book.rating} ★",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF6B4500)
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

            // Horizontally Scrollable Middle Filter Bar (Fixed non-overlapping scrolling!)
            item {
                Spacer(modifier = Modifier.height(14.dp))
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
                            modifier = Modifier.size(15.dp)
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
                val filters = listOf(
                    "ALL" to "All (${books.size})",
                    "RATED" to "Top Rated ★ ($ratedCount)",
                    "BOOKS" to "Books (${books.count { !it.isAudiobook }})",
                    "AUDIOBOOKS" to "Audiobooks (${books.count { it.isAudiobook }})",
                    "CURRENT" to "Reading Now",
                    "WANT" to "Want to Read",
                    "FINISHED" to "Finished",
                    "CHECKED_OUT" to "Checked Out (${books.count { it.isCheckedOut }})"
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

            // Top Rated Banner when viewing RATED filter
            if (currentFilter == "RATED") {
                item {
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
                                    text = "Your Top Rated Books",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF5D4037)
                                )
                                Text(
                                    text = "Ranked strictly from highest personal rating down to least. Affects AI recommendations.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Book List
            if (filteredBooks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching titles found",
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your filter or tap 'Reset All' in the filter menu.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                itemsIndexed(filteredBooks, key = { _, book -> book.id }) { index, book ->
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
                                                text = "Checked Out",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1976D2),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "by ${book.author} (${book.publicationYear})",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

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
                                                    color = Color(0xFF6D4C41)
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = LibraryGold,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "%.1f".format(book.rating),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = " (${book.ratingSource})",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }

                                    Text(
                                        text = "•",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )

                                    Text(
                                        text = if (book.isAudiobook) "Audiobook" else "${book.totalPages} pp",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (book.status == BookStatus.FINISHED.name) "Finished" else "${book.currentChapter} • ${book.progressPercent}%",
                                        fontSize = 11.sp,
                                        color = if (book.status == BookStatus.FINISHED.name) LibraryForestGreen else LibraryAmber,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (book.readingTimeMinutes > 0) {
                                        Text(
                                            text = "${book.readingTimeMinutes}m read",
                                            fontSize = 10.sp,
                                            color = TextMuted
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
}
