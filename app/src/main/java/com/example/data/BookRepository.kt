package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookRepository(private val db: AppDatabase) {
    private val bookDao = db.bookDao()
    private val noteDao = db.noteDao()
    private val friendDao = db.friendDao()
    private val eventDao = db.eventDao()
    private val profileDao = db.userProfileDao()

    val allBooks: Flow<List<BookEntity>> = bookDao.getAllBooks()
    val checkedOutBooks: Flow<List<BookEntity>> = bookDao.getCheckedOutBooks()
    val allFriends: Flow<List<FriendEntity>> = friendDao.getAllFriends()
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    val userProfile: Flow<UserProfileEntity?> = profileDao.getProfile()

    suspend fun getBookById(id: Long): BookEntity? = withContext(Dispatchers.IO) {
        bookDao.getBookByIdSync(id)
    }

    fun getNotesForBook(bookId: Long): Flow<List<NoteEntity>> = noteDao.getNotesForBook(bookId)

    suspend fun insertBook(book: BookEntity): Long = withContext(Dispatchers.IO) {
        bookDao.insertBook(book)
    }

    suspend fun updateBook(book: BookEntity) = withContext(Dispatchers.IO) {
        bookDao.updateBook(book)
    }

    suspend fun deleteBook(book: BookEntity) = withContext(Dispatchers.IO) {
        noteDao.deleteNotesForBook(book.id)
        bookDao.deleteBook(book)
    }

    suspend fun updateReadingProgress(id: Long, page: Int, chapter: String) = withContext(Dispatchers.IO) {
        val book = bookDao.getBookByIdSync(id)
        val newStatus = if (book != null && page >= book.totalPages && book.totalPages > 0) {
            BookStatus.FINISHED.name
        } else {
            book?.status ?: BookStatus.CURRENTLY_READING.name
        }
        if (book != null && newStatus != book.status) {
            bookDao.updateBook(book.copy(currentPage = page, currentChapter = chapter, status = newStatus, lastUpdated = System.currentTimeMillis()))
        } else {
            bookDao.updateReadingProgress(id, page, chapter)
        }
    }

    suspend fun updateAudiobookProgress(id: Long, listenedMinutes: Int) = withContext(Dispatchers.IO) {
        val book = bookDao.getBookByIdSync(id)
        val newStatus = if (book != null && listenedMinutes >= book.totalDurationMinutes && book.totalDurationMinutes > 0) {
            BookStatus.FINISHED.name
        } else {
            book?.status ?: BookStatus.CURRENTLY_READING.name
        }
        if (book != null && newStatus != book.status) {
            bookDao.updateBook(book.copy(listenedMinutes = listenedMinutes, status = newStatus, lastUpdated = System.currentTimeMillis()))
        } else {
            bookDao.updateAudiobookProgress(id, listenedMinutes)
        }
    }

    suspend fun addReadingTime(id: Long, minutes: Int) = withContext(Dispatchers.IO) {
        bookDao.addReadingTime(id, minutes)
    }

    suspend fun insertNote(note: NoteEntity): Long = withContext(Dispatchers.IO) {
        noteDao.insertNote(note)
    }

    suspend fun deleteNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }

    suspend fun searchFriends(query: String): List<FriendEntity> = withContext(Dispatchers.IO) {
        friendDao.searchFriends(query)
    }

    suspend fun addFriend(username: String, name: String, readingTitle: String, progress: Int): Long = withContext(Dispatchers.IO) {
        friendDao.insertFriend(
            FriendEntity(
                username = username,
                displayName = name,
                currentlyReadingTitle = readingTitle,
                currentlyReadingProgress = progress,
                isFriend = true
            )
        )
    }

    suspend fun toggleSaveEvent(id: Long, isSaved: Boolean) = withContext(Dispatchers.IO) {
        eventDao.updateSavedStatus(id, isSaved)
    }

    suspend fun saveProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        profileDao.saveProfile(profile)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingBooks = bookDao.getAllBooks().firstOrNull()
        if (existingBooks.isNullOrEmpty()) {
            val initialBooks = listOf(
                BookEntity(
                    type = ItemType.BOOK.name,
                    title = "Dune",
                    author = "Frank Herbert",
                    synopsis = "Set on the desert planet Arrakis, Dune is the story of the boy Paul Atreides, heir to a noble family tasked with ruling an inhospitable world where the only thing of value is the 'spice' melange.",
                    coverUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80",
                    status = BookStatus.CURRENTLY_READING.name,
                    rating = 4.7,
                    ratingSource = "Goodreads",
                    ratingCount = 1420500,
                    totalPages = 688,
                    currentPage = 280,
                    currentChapter = "Chapter 14: Arrakis Sands",
                    readingTimeMinutes = 340,
                    userRating = 4.8,
                    publicationYear = 1965,
                    genre = "Science Fiction"
                ),
                BookEntity(
                    type = ItemType.BOOK.name,
                    title = "Project Hail Mary",
                    author = "Andy Weir",
                    synopsis = "Ryland Grace is the sole survivor on a desperate, last-chance mission—and if he fails, humanity and the earth itself are doomed.",
                    coverUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&q=80",
                    status = BookStatus.CURRENTLY_READING.name,
                    rating = 4.5,
                    ratingSource = "Amazon",
                    ratingCount = 89040,
                    totalPages = 496,
                    currentPage = 190,
                    currentChapter = "Chapter 9: Eridian Signal",
                    readingTimeMinutes = 210,
                    userRating = 4.7,
                    publicationYear = 2021,
                    genre = "Hard Sci-Fi"
                ),
                BookEntity(
                    type = ItemType.BOOK.name,
                    title = "Tomorrow, and Tomorrow, and Tomorrow",
                    author = "Gabrielle Zevin",
                    synopsis = "A multi-layered novel about two childhood friends who reunite in college to build video game worlds, exploring identity, creativity, and love.",
                    coverUrl = "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=400&q=80",
                    status = BookStatus.WANT_TO_READ.name,
                    rating = 4.6,
                    ratingSource = "Goodreads",
                    ratingCount = 512000,
                    totalPages = 416,
                    currentPage = 0,
                    currentChapter = "Not Started",
                    publicationYear = 2022,
                    genre = "Literary Fiction"
                ),
                BookEntity(
                    type = ItemType.BOOK.name,
                    title = "The Midnight Library",
                    author = "Matt Haig",
                    synopsis = "Between life and death there is a library, and within that library, the shelves go on forever. Every book provides a chance to try another life you could have lived.",
                    coverUrl = "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400&q=80",
                    status = BookStatus.FINISHED.name,
                    rating = 4.1,
                    ratingSource = "Goodreads",
                    ratingCount = 980000,
                    totalPages = 304,
                    currentPage = 304,
                    currentChapter = "Completed",
                    readingTimeMinutes = 390,
                    userRating = 5.0,
                    publicationYear = 2020,
                    genre = "Magical Realism"
                ),
                BookEntity(
                    type = ItemType.BOOK.name,
                    title = "Atomic Habits",
                    author = "James Clear",
                    synopsis = "An easy and proven way to build good habits and break bad ones. Practical strategies on how tiny changes lead to remarkable results.",
                    coverUrl = "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400&q=80",
                    status = BookStatus.CURRENTLY_READING.name,
                    rating = 4.8,
                    ratingSource = "Amazon",
                    ratingCount = 210000,
                    totalPages = 320,
                    currentPage = 145,
                    currentChapter = "Chapter 7: Make It Obvious",
                    readingTimeMinutes = 175,
                    userRating = 4.5,
                    isCheckedOut = true,
                    checkoutLibrary = "San Diego Central Library",
                    dueDate = "Oct 15, 2026",
                    publicationYear = 2018,
                    genre = "Self Improvement"
                ),
                BookEntity(
                    type = ItemType.AUDIOBOOK.name,
                    title = "Born a Crime: Stories from a South African Childhood",
                    author = "Trevor Noah (Narrated by Trevor Noah)",
                    synopsis = "The compelling, inspiring, and comically sublime story of one man's coming-of-age, set during the twilight of apartheid and the tumultuous days of freedom that followed.",
                    coverUrl = "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=400&q=80",
                    status = BookStatus.CURRENTLY_READING.name,
                    rating = 4.9,
                    ratingSource = "Audible",
                    ratingCount = 340000,
                    totalDurationMinutes = 525, // 8h 45m
                    listenedMinutes = 260,
                    currentChapter = "Track 8: Chameleon",
                    readingTimeMinutes = 260,
                    userRating = 4.9,
                    publicationYear = 2016,
                    genre = "Memoir & Audio"
                ),
                BookEntity(
                    type = ItemType.AUDIOBOOK.name,
                    title = "The Song of Achilles",
                    author = "Madeline Miller (Narrated by Frazer Douglas)",
                    synopsis = "A thrilling, profoundly moving, and utterly unique retelling of the legend of Achilles and the Trojan War.",
                    coverUrl = "https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=400&q=80",
                    status = BookStatus.WANT_TO_READ.name,
                    rating = 4.4,
                    ratingSource = "Goodreads",
                    ratingCount = 680000,
                    totalDurationMinutes = 672, // 11h 12m
                    listenedMinutes = 0,
                    currentChapter = "Track 1: Prologue",
                    publicationYear = 2011,
                    genre = "Historical Myth"
                ),
                BookEntity(
                    type = ItemType.AUDIOBOOK.name,
                    title = "Greenlights",
                    author = "Matthew McConaughey",
                    synopsis = "An unconventional memoir filled with raucous stories, outlaw wisdom, and lessons learned the hard way about living with greater satisfaction.",
                    coverUrl = "https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=400&q=80",
                    status = BookStatus.FINISHED.name,
                    rating = 4.7,
                    ratingSource = "Amazon",
                    ratingCount = 180000,
                    totalDurationMinutes = 402, // 6h 42m
                    listenedMinutes = 402,
                    currentChapter = "Completed",
                    readingTimeMinutes = 402,
                    userRating = 4.2,
                    publicationYear = 2020,
                    genre = "Memoir"
                ),
                BookEntity(
                    type = ItemType.BOOK.name,
                    title = "The Silent Patient",
                    author = "Alex Michaelides",
                    synopsis = "Alicia Berenson's life is seemingly perfect. One evening she shoots her husband five times in the face, and then never speaks another word.",
                    coverUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80",
                    status = BookStatus.FINISHED.name,
                    rating = 4.5,
                    ratingSource = "Goodreads",
                    ratingCount = 1200000,
                    totalPages = 336,
                    currentPage = 336,
                    currentChapter = "Completed",
                    readingTimeMinutes = 310,
                    userRating = 4.6,
                    publicationYear = 2019,
                    genre = "Psychological Thriller"
                ),
                BookEntity(
                    type = ItemType.BOOK.name,
                    title = "Book Lovers",
                    author = "Emily Henry",
                    synopsis = "A fiercely ambitious literary agent and a hypercritical book editor cross paths over a summer in North Carolina, challenging every cliché they know.",
                    coverUrl = "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=400&q=80",
                    status = BookStatus.CURRENTLY_READING.name,
                    rating = 4.4,
                    ratingSource = "Goodreads",
                    ratingCount = 890000,
                    totalPages = 384,
                    currentPage = 160,
                    currentChapter = "Chapter 11: The Small Town Rivalry",
                    readingTimeMinutes = 150,
                    userRating = 4.4,
                    publicationYear = 2022,
                    genre = "Romance"
                )
            )
            bookDao.insertBooks(initialBooks)

            // Seed notes
            val insertedBooks = bookDao.getAllBooks().firstOrNull().orEmpty()
            val duneBook = insertedBooks.find { it.title == "Dune" }
            if (duneBook != null) {
                noteDao.insertNote(
                    NoteEntity(
                        bookId = duneBook.id,
                        chapter = "Chapter 14",
                        pageNumber = 275,
                        content = "The litany against fear resonates deeply: 'Fear is the mind-killer.' Need to examine the ecological allegories of the water discipline."
                    )
                )
                noteDao.insertNote(
                    NoteEntity(
                        bookId = duneBook.id,
                        chapter = "Chapter 6",
                        pageNumber = 112,
                        content = "Fascinating description of the Gom Jabbar test of humanity."
                    )
                )
            }
            val hailMary = insertedBooks.find { it.title == "Project Hail Mary" }
            if (hailMary != null) {
                noteDao.insertNote(
                    NoteEntity(
                        bookId = hailMary.id,
                        chapter = "Chapter 9",
                        pageNumber = 185,
                        content = "Rocky using musical chord harmonics to communicate with Grace is peak hard science fiction!"
                    )
                )
            }
        }

        // Friends
        val existingFriends = friendDao.getAllFriends().firstOrNull()
        if (existingFriends.isNullOrEmpty()) {
            val sampleFriends = listOf(
                FriendEntity(
                    username = "sarah_reads",
                    displayName = "Sarah Jenkins",
                    bio = "Reading 50 books this year. Sci-Fi & High Fantasy nerd.",
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&q=80",
                    currentlyReadingTitle = "Dune",
                    currentlyReadingAuthor = "Frank Herbert",
                    currentlyReadingProgress = 68,
                    currentlyReadingChapter = "Chapter 18",
                    currentlyReadingCoverUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80",
                    booksFinishedCount = 28
                ),
                FriendEntity(
                    username = "marcus_pages",
                    displayName = "Marcus Vance",
                    bio = "Audiobook addict during my morning commutes.",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&q=80",
                    currentlyReadingTitle = "Project Hail Mary",
                    currentlyReadingAuthor = "Andy Weir",
                    currentlyReadingProgress = 32,
                    currentlyReadingChapter = "Chapter 6",
                    currentlyReadingCoverUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&q=80",
                    booksFinishedCount = 19
                ),
                FriendEntity(
                    username = "elena_lit",
                    displayName = "Elena Rostova",
                    bio = "Literature professor & weekend book club host.",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&q=80",
                    currentlyReadingTitle = "Tomorrow, and Tomorrow, and Tomorrow",
                    currentlyReadingAuthor = "Gabrielle Zevin",
                    currentlyReadingProgress = 54,
                    currentlyReadingChapter = "Chapter 11",
                    currentlyReadingCoverUrl = "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=400&q=80",
                    booksFinishedCount = 42
                ),
                FriendEntity(
                    username = "jordan_books",
                    displayName = "Jordan Lee",
                    bio = "Tracking non-fiction and cognitive psychology.",
                    avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&q=80",
                    currentlyReadingTitle = "Atomic Habits",
                    currentlyReadingAuthor = "James Clear",
                    currentlyReadingProgress = 85,
                    currentlyReadingChapter = "Chapter 16",
                    currentlyReadingCoverUrl = "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400&q=80",
                    booksFinishedCount = 15
                )
            )
            friendDao.insertFriends(sampleFriends)
        }

        // Events
        val existingEvents = eventDao.getAllEvents().firstOrNull()
        if (existingEvents.isNullOrEmpty()) {
            val sampleEvents = listOf(
                EventEntity(
                    title = "Downtown Library Book Club: Speculative Fiction Night",
                    locationName = "San Diego Central Library",
                    address = "330 Park Blvd, Downtown",
                    city = "San Diego",
                    zipCode = "92101",
                    date = "Thursday, Oct 8",
                    time = "6:30 PM - 8:00 PM",
                    category = "Book Club",
                    description = "Join our monthly speculative fiction gathering! We are discussing Frank Herbert's Dune and modern ecological sci-fi themes. Coffee and refreshments provided.",
                    attendeeCount = 24
                ),
                EventEntity(
                    title = "Author Talk & Signing: New Voices in Mystery",
                    locationName = "Warwick's Books",
                    address = "7812 Girard Ave",
                    city = "San Diego",
                    zipCode = "92037",
                    date = "Saturday, Oct 10",
                    time = "2:00 PM - 3:30 PM",
                    category = "Author Signing",
                    description = "Meet bestselling mystery novelists discussing plot pacing, twists, and character design. Book signing and Q&A immediately following.",
                    attendeeCount = 45
                ),
                EventEntity(
                    title = "Silent Reading & Espresso Morning",
                    locationName = "North Park Library & Coffee Lounge",
                    address = "3795 31st St",
                    city = "San Diego",
                    zipCode = "92104",
                    date = "Sunday, Oct 11",
                    time = "10:00 AM - 12:00 PM",
                    category = "Reading Meetup",
                    description = "Bring whatever book or audiobook you are currently enjoying! 1 hour of uninterrupted quiet reading followed by optional social book recommendations.",
                    attendeeCount = 19
                ),
                EventEntity(
                    title = "Friends of the Public Library Monster Book Sale",
                    locationName = "Balboa Park Activity Hall",
                    address = "2145 Park Blvd",
                    city = "San Diego",
                    zipCode = "92101",
                    date = "Saturday, Oct 17",
                    time = "9:00 AM - 4:00 PM",
                    category = "Book Sale",
                    description = "Over 10,000 gently used books, graphic novels, audiobooks, and vintage editions. All proceeds benefit neighborhood branch programs!",
                    attendeeCount = 112
                ),
                EventEntity(
                    title = "Audiobook Narrator Masterclass & Panel",
                    locationName = "Hillcrest Community Center",
                    address = "3900 Cleveland Ave",
                    city = "San Diego",
                    zipCode = "92103",
                    date = "Wednesday, Oct 21",
                    time = "7:00 PM - 8:30 PM",
                    category = "Workshop",
                    description = "Learn how voice actors bring audiobooks to life. Behind-the-scenes recording techniques, accents, and voice modulation.",
                    attendeeCount = 38
                )
            )
            eventDao.insertEvents(sampleEvents)
        }

        // Profile
        val existingProfile = profileDao.getProfileSync()
        if (existingProfile == null) {
            profileDao.saveProfile(
                UserProfileEntity(
                    id = 1,
                    name = "Morgan Reed",
                    email = "mgloudon@gmail.com",
                    gender = "Reader",
                    age = 27,
                    preferredStyles = "Science Fiction, Mystery, Literary Fiction",
                    bio = "Bibliophile exploring speculative worlds and classic mysteries. Tracking books, audiobooks & library checkouts.",
                    locationCity = "San Diego",
                    locationZip = "92101",
                    cardNumber = "LIB-7842-SD",
                    memberSince = "2024",
                    dailyReadingGoalMinutes = 30
                )
            )
        }
    }
}
