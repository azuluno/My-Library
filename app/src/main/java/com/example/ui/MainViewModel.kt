package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AdaptiveTasteEngine
import com.example.ai.AdaptiveTasteProfile
import com.example.ai.BookAnalysisResult
import com.example.ai.GeminiBookService
import com.example.data.*
import com.example.ui.components.FilterSortState
import com.example.ui.components.SortDirection
import com.example.ui.components.SortField
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab {
    LIBRARY,
    SEARCH_SCAN,
    FRIENDS,
    EVENTS,
    PROFILE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = BookRepository(database)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(MainTab.LIBRARY)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Data streams from repository
    val allBooks: StateFlow<List<BookEntity>> = repository.allBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checkedOutBooks: StateFlow<List<BookEntity>> = repository.checkedOutBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFriends: StateFlow<List<FriendEntity>> = repository.allFriends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<EventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Dynamic AI Taste Profile learning user preferences from Library Card, reading history & ratings
    val adaptiveTasteProfile: StateFlow<AdaptiveTasteProfile> = combine(allBooks, userProfile) { books, profile ->
        AdaptiveTasteEngine.analyzeTaste(books, profile)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AdaptiveTasteProfile(emptyList(), "Eclectic Fiction", "Analyzing reading taste...", 0, 0.0, emptyList())
    )

    // Top Rated Books: All books rated by the user, ordered from highest rating (5.0) down to least
    val topRatedBooks: StateFlow<List<BookEntity>> = allBooks.map { books ->
        books.filter { it.userRating != null }
            .sortedWith(
                compareByDescending<BookEntity> { it.userRating ?: 0.0 }
                    .thenByDescending { it.rating }
                    .thenBy { it.title }
            )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Adaptive Recommendations tailored to learned taste
    private val _adaptiveRecommendations = MutableStateFlow<List<BookAnalysisResult>>(emptyList())
    val adaptiveRecommendations: StateFlow<List<BookAnalysisResult>> = _adaptiveRecommendations.asStateFlow()

    private val _isLoadingRecommendations = MutableStateFlow(false)
    val isLoadingRecommendations: StateFlow<Boolean> = _isLoadingRecommendations.asStateFlow()

    // Library tab filter
    private val _libraryFilter = MutableStateFlow("ALL") // "ALL", "BOOKS", "AUDIOBOOKS", "CURRENT", "WANT", "FINISHED", "CHECKED_OUT"
    val libraryFilter: StateFlow<String> = _libraryFilter.asStateFlow()

    // Advanced Filter and Sort State
    private val _filterSortState = MutableStateFlow(FilterSortState())
    val filterSortState: StateFlow<FilterSortState> = _filterSortState.asStateFlow()

    private val _showFilterSortSheet = MutableStateFlow(false)
    val showFilterSortSheet: StateFlow<Boolean> = _showFilterSortSheet.asStateFlow()

    // Detail & Selection
    private val _selectedBook = MutableStateFlow<BookEntity?>(null)
    val selectedBook: StateFlow<BookEntity?> = _selectedBook.asStateFlow()

    private val _selectedBookNotes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val selectedBookNotes: StateFlow<List<NoteEntity>> = _selectedBookNotes.asStateFlow()

    // Library Card Dialog & Analytics Sheet
    private val _showLibraryCard = MutableStateFlow(false)
    val showLibraryCard: StateFlow<Boolean> = _showLibraryCard.asStateFlow()

    private val _showReadingAnalytics = MutableStateFlow(false)
    val showReadingAnalytics: StateFlow<Boolean> = _showReadingAnalytics.asStateFlow()

    // Add Book Dialog
    private val _showAddBookDialog = MutableStateFlow(false)
    val showAddBookDialog: StateFlow<Boolean> = _showAddBookDialog.asStateFlow()

    // AI Scanner & Search
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scannedResult = MutableStateFlow<BookAnalysisResult?>(null)
    val scannedResult: StateFlow<BookAnalysisResult?> = _scannedResult.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<BookAnalysisResult>>(emptyList())
    val searchResults: StateFlow<List<BookAnalysisResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Reading Timer
    private val _activeTimerBookId = MutableStateFlow<Long?>(null)
    val activeTimerBookId: StateFlow<Long?> = _activeTimerBookId.asStateFlow()

    private val _readingTimerSeconds = MutableStateFlow(0)
    val readingTimerSeconds: StateFlow<Int> = _readingTimerSeconds.asStateFlow()

    private var timerJob: Job? = null

    // Events Refreshing with AI
    private val _isRefreshingEvents = MutableStateFlow(false)
    val isRefreshingEvents: StateFlow<Boolean> = _isRefreshingEvents.asStateFlow()

    // Welcome message 30-second transition to Currently Reading
    private val _showCurrentlyReadingWelcome = MutableStateFlow(false)
    val showCurrentlyReadingWelcome: StateFlow<Boolean> = _showCurrentlyReadingWelcome.asStateFlow()

    // Friend Activity Feed
    private val _activityFeed = MutableStateFlow<List<ActivityFeedItem>>(emptyList())
    val activityFeed: StateFlow<List<ActivityFeedItem>> = _activityFeed.asStateFlow()

    private val _activitySortOrder = MutableStateFlow(SortDirection.DESCENDING)
    val activitySortOrder: StateFlow<SortDirection> = _activitySortOrder.asStateFlow()

    // Neighborhood Checkouts & In-App Direct Messages
    val allCheckouts: StateFlow<List<NeighborhoodCheckoutEntity>> = repository.allCheckouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCheckouts: StateFlow<List<NeighborhoodCheckoutEntity>> = repository.activeCheckouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<NeighborhoodMessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showNotificationsSheet = MutableStateFlow(false)
    val showNotificationsSheet: StateFlow<Boolean> = _showNotificationsSheet.asStateFlow()

    private val _showNfcCheckoutDialog = MutableStateFlow(false)
    val showNfcCheckoutDialog: StateFlow<Boolean> = _showNfcCheckoutDialog.asStateFlow()

    private val _selectedCheckoutForMessage = MutableStateFlow<NeighborhoodCheckoutEntity?>(null)
    val selectedCheckoutForMessage: StateFlow<NeighborhoodCheckoutEntity?> = _selectedCheckoutForMessage.asStateFlow()

    private val _selectedBookForNfcCheckout = MutableStateFlow<BookEntity?>(null)
    val selectedBookForNfcCheckout: StateFlow<BookEntity?> = _selectedBookForNfcCheckout.asStateFlow()

    val pendingAlertsCount: StateFlow<Int> = repository.activeCheckouts
        .map { list -> list.count { it.isOverdue || it.isDueSoon } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Welcome pop-up login on first entering the app if user hasn't created a username
    private val _hasDismissedWelcomeLogin = MutableStateFlow(false)
    val hasDismissedWelcomeLogin: StateFlow<Boolean> = _hasDismissedWelcomeLogin.asStateFlow()

    fun dismissWelcomeLogin() {
        _hasDismissedWelcomeLogin.value = true
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveProfile(current.copy(hasCompletedWelcome = true, isGuest = true))
        }
    }

    fun openWelcomeLogin() {
        _hasDismissedWelcomeLogin.value = false
    }

    fun saveWelcomeLogin(username: String, name: String, email: String, genres: String, followedAuthor: String) {
        viewModelScope.launch {
            val cleanUsername = username.trim().filter { it.isLetterOrDigit() || it == '_' }.take(10)
            val current = userProfile.value ?: UserProfileEntity()
            val existing = current.getFollowedAuthorsList().toMutableList()
            if (followedAuthor.isNotBlank()) {
                val toAdd = followedAuthor.split(",").map { it.trim() }.filter { it.isNotBlank() }
                toAdd.forEach { a ->
                    if (!existing.any { it.equals(a, ignoreCase = true) }) {
                        existing.add(a)
                    }
                }
            }

            val updated = current.copy(
                id = 1,
                username = cleanUsername,
                name = name.ifBlank { "Morgan Reed" },
                email = email.trim(),
                preferredStyles = if (genres.isNotBlank()) genres else current.preferredStyles,
                followedAuthors = existing.joinToString(", "),
                hasCompletedWelcome = true,
                isGuest = cleanUsername.isBlank()
            )
            repository.saveProfile(updated)
            _hasDismissedWelcomeLogin.value = true
            refreshAdaptiveRecommendations()
        }
    }

    fun getMessagesForCheckout(checkoutId: Long): Flow<List<NeighborhoodMessageEntity>> =
        repository.getMessagesForCheckout(checkoutId)

    fun setShowNotificationsSheet(show: Boolean) {
        _showNotificationsSheet.value = show
    }

    fun setShowNfcCheckoutDialog(show: Boolean, book: BookEntity? = null) {
        _selectedBookForNfcCheckout.value = book
        _showNfcCheckoutDialog.value = show
    }

    fun setSelectedCheckoutForMessage(checkout: NeighborhoodCheckoutEntity?) {
        _selectedCheckoutForMessage.value = checkout
    }

    fun onNfcTagScanned(tagId: String) {
        viewModelScope.launch {
            val match = allCheckouts.value.firstOrNull { it.nfcTagId.equals(tagId, ignoreCase = true) && !it.isReturned }
            if (match != null) {
                _selectedCheckoutForMessage.value = match
            } else {
                _showNfcCheckoutDialog.value = true
            }
        }
    }

    fun startNeighborhoodCheckout(
        bookTitle: String,
        bookAuthor: String,
        bookCoverUrl: String,
        personName: String,
        role: CheckoutRole,
        returnDurationDays: Int,
        nfcTagId: String,
        notes: String,
        bookId: Long?
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dueMillis = now + (returnDurationDays.toLong() * 24 * 60 * 60 * 1000)
            val checkout = NeighborhoodCheckoutEntity(
                nfcTagId = nfcTagId,
                bookId = bookId,
                bookTitle = bookTitle,
                bookAuthor = bookAuthor,
                bookCoverUrl = bookCoverUrl,
                personName = personName,
                role = role.name,
                checkoutDateMillis = now,
                returnDurationDays = returnDurationDays,
                dueDateMillis = dueMillis,
                isReturned = false,
                notes = notes
            )
            repository.insertCheckout(checkout)
            _showNfcCheckoutDialog.value = false
        }
    }

    fun markCheckoutReturned(checkoutId: Long) {
        viewModelScope.launch {
            repository.markCheckoutReturned(checkoutId)
        }
    }

    fun deleteCheckout(checkout: NeighborhoodCheckoutEntity) {
        viewModelScope.launch {
            repository.deleteCheckout(checkout)
        }
    }

    fun sendNeighborhoodMessage(checkoutId: Long, text: String) {
        viewModelScope.launch {
            val checkout = allCheckouts.value.firstOrNull { it.id == checkoutId }
            val myName = userProfile.value?.name ?: "Morgan"
            val recipient = checkout?.personName ?: "Neighbor"
            repository.sendNeighborhoodMessage(
                checkoutId = checkoutId,
                senderName = myName,
                recipientName = recipient,
                text = text
            )
        }
    }

    fun toggleFollowAuthor(authorName: String) {
        viewModelScope.launch {
            repository.toggleFollowAuthor(authorName)
            if (_searchResults.value.isNotEmpty()) {
                val taste = adaptiveTasteProfile.value
                _searchResults.value = AdaptiveTasteEngine.rankResults(_searchResults.value, taste)
            }
            refreshAdaptiveRecommendations()
        }
    }

    fun setAutoDeleteMessages(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoDeleteMessages(enabled)
        }
    }

    fun clearAllNeighborhoodMessages() {
        viewModelScope.launch {
            repository.clearAllNeighborhoodMessages()
        }
    }

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            seedActivityFeed()
            // Prefetch Gemini adaptive recommendations tailored to initial taste
            refreshAdaptiveRecommendations()
        }

        // 30 seconds timer: switch from welcome to currently reading if user has an active book
        viewModelScope.launch {
            delay(30_000)
            _showCurrentlyReadingWelcome.value = true
        }
    }

    private fun seedActivityFeed() {
        val now = System.currentTimeMillis()
        _activityFeed.value = listOf(
            ActivityFeedItem(
                id = "act-1",
                friendUsername = "sarah_reads",
                friendName = "Sarah Jenkins",
                friendAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&q=80",
                actionType = "FINISHED",
                bookTitle = "The Midnight Library",
                bookAuthor = "Matt Haig",
                bookCover = "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400&q=80",
                rating = 5.0,
                noteSnippet = "An absolute masterpiece on regret and appreciating the life you have! 5/5 stars.",
                timestamp = now - (2 * 3600 * 1000), // 2 hours ago
                likesCount = 8,
                comments = listOf(
                    ActivityComment(author = "marcus_pages", text = "Added to my reading list! Matt Haig is fantastic.", timeAgo = "1h ago")
                )
            ),
            ActivityFeedItem(
                id = "act-2",
                friendUsername = "marcus_pages",
                friendName = "Marcus Vance",
                friendAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&q=80",
                actionType = "READING",
                bookTitle = "Project Hail Mary",
                bookAuthor = "Andy Weir",
                bookCover = "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&q=80",
                progressPercent = 32,
                currentChapter = "Chapter 6: First Contact",
                timestamp = now - (6 * 3600 * 1000), // 6 hours ago
                likesCount = 5,
                comments = emptyList()
            ),
            ActivityFeedItem(
                id = "act-3",
                friendUsername = "elena_lit",
                friendName = "Elena Rostova",
                friendAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&q=80",
                actionType = "WANT_TO_READ",
                bookTitle = "Tomorrow, and Tomorrow, and Tomorrow",
                bookAuthor = "Gabrielle Zevin",
                bookCover = "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=400&q=80",
                timestamp = now - (24 * 3600 * 1000), // 1 day ago
                likesCount = 12,
                comments = listOf(
                    ActivityComment(author = "sarah_reads", text = "You will love this one! Incredible character work.", timeAgo = "20h ago")
                )
            ),
            ActivityFeedItem(
                id = "act-4",
                friendUsername = "jordan_books",
                friendName = "Jordan Lee",
                friendAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&q=80",
                actionType = "FINISHED",
                bookTitle = "Atomic Habits",
                bookAuthor = "James Clear",
                bookCover = "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400&q=80",
                rating = 4.8,
                noteSnippet = "Implemented the two-minute rule immediately. Highly actionable guide.",
                timestamp = now - (48 * 3600 * 1000), // 2 days ago
                likesCount = 14,
                comments = emptyList()
            )
        )
    }

    fun toggleLikeActivity(activityId: String) {
        _activityFeed.value = _activityFeed.value.map { item ->
            if (item.id == activityId) {
                val newLiked = !item.isLikedByMe
                val newCount = if (newLiked) item.likesCount + 1 else (item.likesCount - 1).coerceAtLeast(0)
                item.copy(isLikedByMe = newLiked, likesCount = newCount)
            } else {
                item
            }
        }
    }

    fun addCommentToActivity(activityId: String, commentText: String) {
        if (commentText.isBlank()) return
        val currentUserName = userProfile.value?.displayUserName ?: "User"
        val newComment = ActivityComment(author = currentUserName, text = commentText.trim(), timeAgo = "Just now")
        _activityFeed.value = _activityFeed.value.map { item ->
            if (item.id == activityId) {
                item.copy(comments = item.comments + newComment)
            } else {
                item
            }
        }
    }

    fun toggleActivitySortOrder() {
        val newOrder = if (_activitySortOrder.value == SortDirection.DESCENDING) SortDirection.ASCENDING else SortDirection.DESCENDING
        _activitySortOrder.value = newOrder
        _activityFeed.value = if (newOrder == SortDirection.DESCENDING) {
            _activityFeed.value.sortedByDescending { it.timestamp }
        } else {
            _activityFeed.value.sortedBy { it.timestamp }
        }
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setLibraryFilter(filter: String) {
        _libraryFilter.value = filter
    }

    fun setFilterSortState(state: FilterSortState) {
        _filterSortState.value = state
    }

    fun resetFilterSortState() {
        _filterSortState.value = FilterSortState()
    }

    fun setShowFilterSortSheet(show: Boolean) {
        _showFilterSortSheet.value = show
    }

    fun setShowReadingAnalytics(show: Boolean) {
        _showReadingAnalytics.value = show
    }

    fun selectBook(book: BookEntity?) {
        _selectedBook.value = book
        if (book != null) {
            viewModelScope.launch {
                repository.getNotesForBook(book.id).collect { notes ->
                    _selectedBookNotes.value = notes
                }
            }
        } else {
            _selectedBookNotes.value = emptyList()
        }
    }

    fun setShowLibraryCard(show: Boolean) {
        _showLibraryCard.value = show
    }

    fun setShowAddBookDialog(show: Boolean) {
        _showAddBookDialog.value = show
    }

    fun updateReadingProgress(bookId: Long, page: Int, chapter: String) {
        viewModelScope.launch {
            repository.updateReadingProgress(bookId, page, chapter)
            if (_selectedBook.value?.id == bookId) {
                _selectedBook.value = repository.getBookById(bookId)
            }
        }
    }

    fun updateAudiobookProgress(bookId: Long, listenedMinutes: Int) {
        viewModelScope.launch {
            repository.updateAudiobookProgress(bookId, listenedMinutes)
            if (_selectedBook.value?.id == bookId) {
                _selectedBook.value = repository.getBookById(bookId)
            }
        }
    }

    fun startReadingTimer(bookId: Long) {
        if (_activeTimerBookId.value == bookId && timerJob?.isActive == true) return
        stopReadingTimer()
        _activeTimerBookId.value = bookId
        _readingTimerSeconds.value = 0
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _readingTimerSeconds.value += 1
            }
        }
    }

    fun stopReadingTimer() {
        val bookId = _activeTimerBookId.value
        val seconds = _readingTimerSeconds.value
        timerJob?.cancel()
        timerJob = null
        _activeTimerBookId.value = null
        _readingTimerSeconds.value = 0

        if (bookId != null && seconds >= 30) {
            val minutes = (seconds + 30) / 60
            viewModelScope.launch {
                repository.addReadingTime(bookId, minutes)
                if (_selectedBook.value?.id == bookId) {
                    _selectedBook.value = repository.getBookById(bookId)
                }
            }
        }
    }

    fun addNote(bookId: Long, chapter: String, page: Int?, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.insertNote(
                NoteEntity(
                    bookId = bookId,
                    chapter = chapter,
                    pageNumber = page,
                    content = content
                )
            )
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun addBook(book: BookEntity) {
        viewModelScope.launch {
            repository.insertBook(book)
            if (book.status == BookStatus.CURRENTLY_READING.name) {
                _showCurrentlyReadingWelcome.value = true
            }
        }
    }

    fun updateBook(book: BookEntity) {
        viewModelScope.launch {
            repository.updateBook(book)
            if (_selectedBook.value?.id == book.id) {
                _selectedBook.value = book
            }
        }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            if (_selectedBook.value?.id == book.id) {
                _selectedBook.value = null
            }
            repository.deleteBook(book)
        }
    }

    fun scanCoverImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val result = GeminiBookService.analyzeBookCover(bitmap)
                _scannedResult.value = result
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun clearScannedResult() {
        _scannedResult.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateUserRating(bookId: Long, rating: Double) {
        viewModelScope.launch {
            val book = repository.getBookById(bookId)
            if (book != null) {
                val updated = book.copy(userRating = rating, lastUpdated = System.currentTimeMillis())
                repository.updateBook(updated)
                if (_selectedBook.value?.id == bookId) {
                    _selectedBook.value = updated
                }
                // Rate change affects AI taste learning and recommendations immediately
                refreshAdaptiveRecommendations()
            }
        }
    }

    fun refreshAdaptiveRecommendations() {
        viewModelScope.launch {
            _isLoadingRecommendations.value = true
            try {
                val taste = adaptiveTasteProfile.value
                val recs = GeminiBookService.fetchAdaptiveRecommendations(
                    userTasteSummary = taste.evolvingSummary,
                    topGenres = taste.topGenres.map { it.genre }
                )
                _adaptiveRecommendations.value = recs
            } catch (e: Exception) {
                // Keep existing or fallback
            } finally {
                _isLoadingRecommendations.value = false
            }
        }
    }

    fun searchBooks(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val taste = adaptiveTasteProfile.value
                val results = GeminiBookService.searchOrAutoCompleteBook(query, taste.evolvingSummary)
                // Rank results using AdaptiveTasteEngine so genres matching learned preferences appear first
                _searchResults.value = AdaptiveTasteEngine.rankResults(results, taste)
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun addScannedOrSearchResultToLibrary(
        result: BookAnalysisResult,
        status: BookStatus,
        type: ItemType,
        isCheckedOut: Boolean = false,
        libraryName: String? = null,
        dueDate: String? = null
    ) {
        viewModelScope.launch {
            val book = BookEntity(
                type = type.name,
                title = result.title,
                author = result.author,
                synopsis = result.synopsis,
                coverUrl = result.coverUrl.ifBlank { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80" },
                status = status.name,
                rating = result.rating,
                ratingSource = result.ratingSource,
                ratingCount = result.ratingCount,
                totalPages = result.totalPages,
                currentPage = if (status == BookStatus.FINISHED) result.totalPages else 0,
                totalDurationMinutes = if (type == ItemType.AUDIOBOOK) 480 else 0,
                isCheckedOut = isCheckedOut,
                checkoutLibrary = libraryName,
                dueDate = dueDate,
                genre = result.genre
            )
            repository.insertBook(book)
            if (status == BookStatus.CURRENTLY_READING) {
                _showCurrentlyReadingWelcome.value = true
            }
            _scannedResult.value = null
            // Adding a new book updates the user's reading trends and AI recommendations
            refreshAdaptiveRecommendations()
        }
    }

    fun addFriend(username: String) {
        viewModelScope.launch {
            val cleanUsername = username.removePrefix("@").trim()
            val cleanName = cleanUsername.replaceFirstChar { it.uppercase() }
            repository.addFriend(
                username = cleanUsername,
                name = cleanName,
                readingTitle = "Dune",
                progress = (30..80).random()
            )
        }
    }

    fun addFullFriend(
        username: String,
        displayName: String,
        readingTitle: String,
        progress: Int = 25,
        chapter: String = "Chapter 1"
    ) {
        viewModelScope.launch {
            val cleanUsername = username.removePrefix("@").trim()
            val cleanName = displayName.trim().ifBlank { cleanUsername.replaceFirstChar { it.uppercase() } }
            val cleanTitle = readingTitle.trim().ifBlank { "The Midnight Library" }
            repository.addFriendWithDetails(
                username = cleanUsername,
                name = cleanName,
                readingTitle = cleanTitle,
                progress = progress.coerceIn(0, 100),
                chapter = chapter.ifBlank { "Chapter 1" }
            )
            val newFeedItem = ActivityFeedItem(
                id = "act-${System.currentTimeMillis()}",
                friendUsername = cleanUsername,
                friendName = cleanName,
                friendAvatar = "",
                actionType = "READING",
                bookTitle = cleanTitle,
                bookAuthor = "Matt Haig",
                bookCover = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80",
                progressPercent = progress.coerceIn(0, 100),
                currentChapter = chapter.ifBlank { "Chapter 1" },
                timestamp = System.currentTimeMillis(),
                likesCount = 0,
                comments = emptyList()
            )
            _activityFeed.value = listOf(newFeedItem) + _activityFeed.value
        }
    }

    fun toggleSaveEvent(eventId: Long, isSaved: Boolean) {
        viewModelScope.launch {
            repository.toggleSaveEvent(eventId, isSaved)
        }
    }

    fun saveUserProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            val cleanUsername = profile.username.trim().filter { it.isLetterOrDigit() || it == '_' }.take(10)
            val updated = profile.copy(
                username = cleanUsername,
                hasCompletedWelcome = true,
                isGuest = cleanUsername.isBlank()
            )
            repository.saveProfile(updated)
            refreshAdaptiveRecommendations()
        }
    }

    fun refreshEventsWithAi(city: String, zip: String) {
        viewModelScope.launch {
            _isRefreshingEvents.value = true
            try {
                val events = GeminiBookService.discoverLocalEvents(city, zip)
                database.eventDao().insertEvents(events)
            } finally {
                _isRefreshingEvents.value = false
            }
        }
    }
}
