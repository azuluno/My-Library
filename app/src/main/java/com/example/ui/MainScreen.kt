package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BookStatus
import com.example.ui.components.AddBookDialog
import com.example.ui.components.BookDetailSheet
import com.example.ui.components.FilterSortBottomSheet
import com.example.ui.components.LibraryCardDialog
import com.example.ui.components.ReadingAnalyticsSheet
import com.example.ui.screens.*
import com.example.ui.theme.LibraryForestGreen
import com.example.ui.theme.LibraryGold

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val books by viewModel.allBooks.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val friends by viewModel.allFriends.collectAsStateWithLifecycle()
    val events by viewModel.allEvents.collectAsStateWithLifecycle()
    val libraryFilter by viewModel.libraryFilter.collectAsStateWithLifecycle()

    val filterSortState by viewModel.filterSortState.collectAsStateWithLifecycle()
    val showFilterSortSheet by viewModel.showFilterSortSheet.collectAsStateWithLifecycle()
    val showReadingAnalytics by viewModel.showReadingAnalytics.collectAsStateWithLifecycle()
    val showCurrentlyReadingWelcome by viewModel.showCurrentlyReadingWelcome.collectAsStateWithLifecycle()

    val activityFeed by viewModel.activityFeed.collectAsStateWithLifecycle()
    val activitySortOrder by viewModel.activitySortOrder.collectAsStateWithLifecycle()

    val selectedBook by viewModel.selectedBook.collectAsStateWithLifecycle()
    val selectedBookNotes by viewModel.selectedBookNotes.collectAsStateWithLifecycle()
    val showLibraryCard by viewModel.showLibraryCard.collectAsStateWithLifecycle()
    val showAddBookDialog by viewModel.showAddBookDialog.collectAsStateWithLifecycle()

    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scannedResult by viewModel.scannedResult.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    val activeTimerBookId by viewModel.activeTimerBookId.collectAsStateWithLifecycle()
    val readingTimerSeconds by viewModel.readingTimerSeconds.collectAsStateWithLifecycle()
    val isRefreshingEvents by viewModel.isRefreshingEvents.collectAsStateWithLifecycle()

    val adaptiveTasteProfile by viewModel.adaptiveTasteProfile.collectAsStateWithLifecycle()
    val topRatedBooks by viewModel.topRatedBooks.collectAsStateWithLifecycle()
    val adaptiveRecommendations by viewModel.adaptiveRecommendations.collectAsStateWithLifecycle()
    val isLoadingRecommendations by viewModel.isLoadingRecommendations.collectAsStateWithLifecycle()

    // Handle back press gracefully
    BackHandler(
        enabled = selectedBook != null || showLibraryCard || showAddBookDialog ||
                showFilterSortSheet || showReadingAnalytics || currentTab != MainTab.LIBRARY
    ) {
        when {
            selectedBook != null -> viewModel.selectBook(null)
            showFilterSortSheet -> viewModel.setShowFilterSortSheet(false)
            showReadingAnalytics -> viewModel.setShowReadingAnalytics(false)
            showLibraryCard -> viewModel.setShowLibraryCard(false)
            showAddBookDialog -> viewModel.setShowAddBookDialog(false)
            currentTab != MainTab.LIBRARY -> viewModel.selectTab(MainTab.LIBRARY)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF6F3EC),
                contentColor = LibraryForestGreen,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == MainTab.LIBRARY,
                    onClick = { viewModel.selectTab(MainTab.LIBRARY) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.LIBRARY) Icons.Default.LocalLibrary else Icons.Outlined.LocalLibrary,
                            contentDescription = "My Library"
                        )
                    },
                    label = { Text("Library", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LibraryForestGreen,
                        selectedTextColor = LibraryForestGreen,
                        indicatorColor = LibraryGold.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.testTag("nav_library_tab")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.SEARCH_SCAN,
                    onClick = { viewModel.selectTab(MainTab.SEARCH_SCAN) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.SEARCH_SCAN) Icons.Default.CameraAlt else Icons.Outlined.CameraAlt,
                            contentDescription = "Search & AI Scan"
                        )
                    },
                    label = { Text("AI Scan", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LibraryForestGreen,
                        selectedTextColor = LibraryForestGreen,
                        indicatorColor = LibraryGold.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.testTag("nav_search_scan_tab")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.FRIENDS,
                    onClick = { viewModel.selectTab(MainTab.FRIENDS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.FRIENDS) Icons.Default.People else Icons.Outlined.People,
                            contentDescription = "Friends"
                        )
                    },
                    label = { Text("Community", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LibraryForestGreen,
                        selectedTextColor = LibraryForestGreen,
                        indicatorColor = LibraryGold.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.testTag("nav_friends_tab")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.EVENTS,
                    onClick = { viewModel.selectTab(MainTab.EVENTS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.EVENTS) Icons.Default.Event else Icons.Outlined.Event,
                            contentDescription = "Events"
                        )
                    },
                    label = { Text("Events", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LibraryForestGreen,
                        selectedTextColor = LibraryForestGreen,
                        indicatorColor = LibraryGold.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.testTag("nav_events_tab")
                )

                // User / Username Tab: Shows "User" until user specifies username (max 6 letters/digits)
                val userLabel = userProfile?.displayUserName ?: "User"
                NavigationBarItem(
                    selected = currentTab == MainTab.PROFILE,
                    onClick = { viewModel.selectTab(MainTab.PROFILE) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.PROFILE) Icons.Default.Person else Icons.Outlined.Person,
                            contentDescription = userLabel
                        )
                    },
                    label = {
                        Text(
                            text = userLabel,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LibraryForestGreen,
                        selectedTextColor = LibraryForestGreen,
                        indicatorColor = LibraryGold.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.testTag("nav_profile_tab")
                )
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.LIBRARY -> LibraryScreen(
                    books = books,
                    userProfile = userProfile,
                    currentFilter = libraryFilter,
                    filterSortState = filterSortState,
                    showCurrentlyReadingWelcome = showCurrentlyReadingWelcome,
                    adaptiveTasteProfile = adaptiveTasteProfile,
                    topRatedBooks = topRatedBooks,
                    adaptiveRecommendations = adaptiveRecommendations,
                    isLoadingRecommendations = isLoadingRecommendations,
                    onFilterChange = { viewModel.setLibraryFilter(it) },
                    onSelectBook = { viewModel.selectBook(it) },
                    onOpenLibraryCard = { viewModel.setShowLibraryCard(true) },
                    onOpenFilterSort = { viewModel.setShowFilterSortSheet(true) },
                    onOpenAnalytics = { viewModel.setShowReadingAnalytics(true) },
                    onAddNewBook = { viewModel.setShowAddBookDialog(true) },
                    onRefreshRecommendations = { viewModel.refreshAdaptiveRecommendations() },
                    onAddRecommendationToLibrary = { rec ->
                        viewModel.addScannedOrSearchResultToLibrary(rec, BookStatus.WANT_TO_READ, com.example.data.ItemType.BOOK)
                    }
                )
                MainTab.SEARCH_SCAN -> SearchScanScreen(
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    isSearching = isSearching,
                    isScanning = isScanning,
                    scannedResult = scannedResult,
                    adaptiveTasteProfile = adaptiveTasteProfile,
                    adaptiveRecommendations = adaptiveRecommendations,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onSearchSubmit = { viewModel.searchBooks(it) },
                    onScanImage = { viewModel.scanCoverImage(it) },
                    onClearScannedResult = { viewModel.clearScannedResult() },
                    onAddToLibrary = { result, status, type, isCheckedOut, lib, due ->
                        viewModel.addScannedOrSearchResultToLibrary(result, status, type, isCheckedOut, lib, due)
                    },
                    onRefreshRecommendations = { viewModel.refreshAdaptiveRecommendations() }
                )
                MainTab.FRIENDS -> FriendsScreen(
                    friends = friends,
                    userBooks = books,
                    activityFeed = activityFeed,
                    activitySortOrder = activitySortOrder,
                    onAddFriend = { viewModel.addFriend(it) },
                    onLikeActivity = { viewModel.toggleLikeActivity(it) },
                    onAddComment = { id, text -> viewModel.addCommentToActivity(id, text) },
                    onToggleActivitySort = { viewModel.toggleActivitySortOrder() }
                )
                MainTab.EVENTS -> EventsScreen(
                    events = events,
                    userProfile = userProfile,
                    isRefreshing = isRefreshingEvents,
                    onToggleSave = { id, saved -> viewModel.toggleSaveEvent(id, saved) },
                    onRefreshWithAi = { city, zip -> viewModel.refreshEventsWithAi(city, zip) },
                    onNavigateToPreferences = { viewModel.selectTab(MainTab.PROFILE) }
                )
                MainTab.PROFILE -> ProfileScreen(
                    profile = userProfile,
                    books = books,
                    adaptiveTasteProfile = adaptiveTasteProfile,
                    onViewTopRated = {
                        viewModel.selectTab(MainTab.LIBRARY)
                        viewModel.setLibraryFilter("RATED")
                    },
                    onOpenLibraryCard = { viewModel.setShowLibraryCard(true) },
                    onOpenAnalytics = { viewModel.setShowReadingAnalytics(true) },
                    onSaveProfile = { viewModel.saveUserProfile(it) }
                )
            }
        }
    }

    // Modal Sheet: Book Detail
    selectedBook?.let { book ->
        BookDetailSheet(
            book = book,
            notes = selectedBookNotes,
            activeTimerBookId = activeTimerBookId,
            timerSeconds = readingTimerSeconds,
            onDismiss = { viewModel.selectBook(null) },
            onUpdateProgress = { page, chapter ->
                viewModel.updateReadingProgress(book.id, page, chapter)
            },
            onUpdateAudioProgress = { listenedMins ->
                viewModel.updateAudiobookProgress(book.id, listenedMins)
            },
            onStartTimer = { viewModel.startReadingTimer(book.id) },
            onStopTimer = { viewModel.stopReadingTimer() },
            onAddNote = { chapter, page, text ->
                viewModel.addNote(book.id, chapter, page, text)
            },
            onDeleteNote = { note ->
                viewModel.deleteNote(note)
            },
            onDeleteBook = { b ->
                viewModel.deleteBook(b)
            },
            onUpdateStatus = { status ->
                viewModel.updateBook(book.copy(status = status.name, lastUpdated = System.currentTimeMillis()))
            },
            onUpdateUserRating = { rating ->
                viewModel.updateUserRating(book.id, rating)
            }
        )
    }

    // Sheet: Advanced Filter & Sort
    if (showFilterSortSheet) {
        FilterSortBottomSheet(
            currentState = filterSortState,
            availableBooks = books,
            onApply = { viewModel.setFilterSortState(it) },
            onReset = { viewModel.resetFilterSortState() },
            onDismiss = { viewModel.setShowFilterSortSheet(false) }
        )
    }

    // Sheet: Reading Progress Visualizations
    if (showReadingAnalytics) {
        ReadingAnalyticsSheet(
            books = books,
            userProfile = userProfile,
            onDismiss = { viewModel.setShowReadingAnalytics(false) }
        )
    }

    // Dialog: Library Card
    if (showLibraryCard) {
        LibraryCardDialog(
            profile = userProfile,
            books = books,
            adaptiveTasteProfile = adaptiveTasteProfile,
            onViewTopRated = {
                viewModel.setShowLibraryCard(false)
                viewModel.selectTab(MainTab.LIBRARY)
                viewModel.setLibraryFilter("RATED")
            },
            onDismiss = { viewModel.setShowLibraryCard(false) }
        )
    }

    // Dialog: Add Book
    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { viewModel.setShowAddBookDialog(false) },
            onSaveBook = { book -> viewModel.addBook(book) }
        )
    }
}
