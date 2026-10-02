package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

enum class CheckoutRole {
    LENT_TO_FRIEND,        // I checked out this book from my library to a friend/neighbor
    BORROWED_FROM_NEIGHBOR // I checked out / borrowed this book from a fellow user/neighbor
}

enum class ReturnDuration(val days: Int, val label: String) {
    SEVEN_DAYS(7, "7 Days"),
    TWO_WEEKS(14, "2 Weeks"),
    ONE_MONTH(30, "1 Month"),
    THREE_MONTHS(90, "3 Months (Max)");

    companion object {
        fun fromDays(days: Int): ReturnDuration =
            entries.firstOrNull { it.days == days } ?: TWO_WEEKS
    }
}

@Entity(tableName = "neighborhood_checkouts")
data class NeighborhoodCheckoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nfcTagId: String,               // NFC tag scanned, e.g. "NFC-8492-BK"
    val bookId: Long? = null,           // Linked book ID if from user's personal library
    val bookTitle: String,
    val bookAuthor: String,
    val bookCoverUrl: String = "",
    val personName: String,             // Friend / neighbor name
    val role: String = CheckoutRole.LENT_TO_FRIEND.name,
    val checkoutDateMillis: Long = System.currentTimeMillis(),
    val returnDurationDays: Int = 14,   // 7, 14, 30, 90 (max 3 months)
    val dueDateMillis: Long = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000),
    val isReturned: Boolean = false,
    val notes: String = ""
) {
    val isLentToFriend: Boolean
        get() = role == CheckoutRole.LENT_TO_FRIEND.name

    val isBorrowedFromNeighbor: Boolean
        get() = role == CheckoutRole.BORROWED_FROM_NEIGHBOR.name

    val isOverdue: Boolean
        get() = !isReturned && System.currentTimeMillis() > dueDateMillis

    val daysRemaining: Int
        get() {
            val diff = dueDateMillis - System.currentTimeMillis()
            return (diff / (1000L * 60 * 60 * 24)).toInt()
        }

    val isDueSoon: Boolean
        get() = !isReturned && daysRemaining in 0..3
}

@Entity(tableName = "neighborhood_messages")
data class NeighborhoodMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val checkoutId: Long,
    val senderName: String,
    val recipientName: String,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = true
)

@Dao
interface NeighborhoodCheckoutDao {
    @Query("SELECT * FROM neighborhood_checkouts ORDER BY isReturned ASC, dueDateMillis ASC")
    fun getAllCheckouts(): Flow<List<NeighborhoodCheckoutEntity>>

    @Query("SELECT * FROM neighborhood_checkouts WHERE isReturned = 0 ORDER BY dueDateMillis ASC")
    fun getActiveCheckouts(): Flow<List<NeighborhoodCheckoutEntity>>

    @Query("SELECT * FROM neighborhood_checkouts WHERE nfcTagId = :tagId LIMIT 1")
    suspend fun getCheckoutByNfcTag(tagId: String): NeighborhoodCheckoutEntity?

    @Query("SELECT * FROM neighborhood_checkouts WHERE id = :id LIMIT 1")
    suspend fun getCheckoutById(id: Long): NeighborhoodCheckoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckout(checkout: NeighborhoodCheckoutEntity): Long

    @Update
    suspend fun updateCheckout(checkout: NeighborhoodCheckoutEntity)

    @Delete
    suspend fun deleteCheckout(checkout: NeighborhoodCheckoutEntity)

    @Query("DELETE FROM neighborhood_checkouts WHERE isReturned = 1")
    suspend fun deleteReturnedCheckouts()
}

@Dao
interface NeighborhoodMessageDao {
    @Query("SELECT * FROM neighborhood_messages WHERE checkoutId = :checkoutId ORDER BY timestampMillis ASC")
    fun getMessagesForCheckout(checkoutId: Long): Flow<List<NeighborhoodMessageEntity>>

    @Query("SELECT * FROM neighborhood_messages ORDER BY timestampMillis DESC")
    fun getAllMessages(): Flow<List<NeighborhoodMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: NeighborhoodMessageEntity): Long

    @Query("DELETE FROM neighborhood_messages WHERE checkoutId = :checkoutId")
    suspend fun deleteMessagesForCheckout(checkoutId: Long)

    @Query("DELETE FROM neighborhood_messages")
    suspend fun deleteAllMessages()
}
