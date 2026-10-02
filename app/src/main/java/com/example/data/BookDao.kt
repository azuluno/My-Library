package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY lastUpdated DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE type = :type ORDER BY lastUpdated DESC")
    fun getBooksByType(type: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE status = :status ORDER BY lastUpdated DESC")
    fun getBooksByStatus(status: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isCheckedOut = 1 ORDER BY lastUpdated DESC")
    fun getCheckedOutBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    fun getBookById(id: Long): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookByIdSync(id: Long): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: Long)

    @Query("UPDATE books SET currentPage = :currentPage, currentChapter = :chapter, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateReadingProgress(id: Long, currentPage: Int, chapter: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE books SET listenedMinutes = :listenedMinutes, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateAudiobookProgress(id: Long, listenedMinutes: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE books SET readingTimeMinutes = readingTimeMinutes + :minutes, lastUpdated = :timestamp WHERE id = :id")
    suspend fun addReadingTime(id: Long, minutes: Int, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM book_notes WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getNotesForBook(bookId: Long): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM book_notes WHERE bookId = :bookId")
    suspend fun deleteNotesForBook(bookId: Long)
}

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends ORDER BY isFriend DESC, displayName ASC")
    fun getAllFriends(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE username LIKE '%' || :query || '%' OR displayName LIKE '%' || :query || '%'")
    suspend fun searchFriends(query: String): List<FriendEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendEntity>)

    @Query("UPDATE friends SET isFriend = :isFriend WHERE id = :id")
    suspend fun updateFriendStatus(id: Long, isFriend: Boolean)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY id ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Query("UPDATE events SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateSavedStatus(id: Long, isSaved: Boolean)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfileEntity)
}
