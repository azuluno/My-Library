package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailSheet(
    book: BookEntity,
    notes: List<NoteEntity>,
    activeTimerBookId: Long?,
    timerSeconds: Int,
    onDismiss: () -> Unit,
    onUpdateProgress: (page: Int, chapter: String) -> Unit,
    onUpdateAudioProgress: (listenedMinutes: Int) -> Unit,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onAddNote: (chapter: String, page: Int?, content: String) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onDeleteBook: (BookEntity) -> Unit,
    onUpdateStatus: (BookStatus) -> Unit,
    onUpdateUserRating: (Double) -> Unit,
    isAuthorFollowed: Boolean = false,
    onToggleFollowAuthor: (String) -> Unit = {},
    onStartNfcCheckout: (BookEntity) -> Unit = {}
) {
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var editPageText by remember(book.currentPage) { mutableStateOf(book.currentPage.toString()) }
    var editChapterText by remember(book.currentChapter) { mutableStateOf(book.currentChapter) }
    var editAudioMinutesText by remember(book.listenedMinutes) { mutableStateOf(book.listenedMinutes.toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFBF9F5),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("book_detail_bottom_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header: Cover & Main Info
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Book Cover
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(width = 96.dp, height = 138.dp)
                    ) {
                        if (book.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = book.coverUrl,
                                contentDescription = "${book.title} cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(LibraryForestGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (book.isAudiobook) Icons.Default.Headphones else Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = LibraryGold,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // Badges
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (book.isAudiobook) LibraryAmber.copy(alpha = 0.15f) else LibraryForestGreen.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (book.isAudiobook) Icons.Default.Headphones else Icons.Default.Book,
                                        contentDescription = null,
                                        tint = if (book.isAudiobook) LibraryAmber else LibraryForestGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (book.isAudiobook) "Audiobook" else "Book",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (book.isAudiobook) LibraryAmber else LibraryForestGreen
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEDE8DE)
                            ) {
                                Text(
                                    text = book.genre,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = book.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "by ${book.author}",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onToggleFollowAuthor(book.author) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("detail_follow_author_heart")
                            ) {
                                Icon(
                                    imageVector = if (isAuthorFollowed) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isAuthorFollowed) "Unfollow Author" else "Follow Author",
                                    tint = if (isAuthorFollowed) Color(0xFFE91E63) else Color.Gray,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            if (isAuthorFollowed) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFEBEE)
                                ) {
                                    Text(
                                        text = "Followed",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC2185B),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Online Rating Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFF7E6))
                                .border(1.dp, Color(0xFFFFE0A3), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFE5A93C),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1f".format(book.rating),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF6B4500)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "on ${book.ratingSource}",
                                fontSize = 11.sp,
                                color = Color(0xFF8B6420)
                            )
                        }
                    }
                }
            }

            // Checked out indicator if applicable
            if (book.isCheckedOut) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE3F2FD),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF90CAF9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalLibrary,
                                contentDescription = null,
                                tint = Color(0xFF1976D2)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Checked Out from ${book.checkoutLibrary ?: "Local Library"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0D47A1)
                                )
                                Text(
                                    text = "Due Date: ${book.dueDate ?: "Upcoming"}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1565C0)
                                )
                            }
                        }
                    }
                }
            }

            // NFC Tag & Neighborhood Lending Action Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedCard(
                    onClick = { onStartNfcCheckout(book) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFF6F8F5)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6E4D6)),
                    modifier = Modifier.fillMaxWidth().testTag("detail_nfc_checkout_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = LibraryForestGreen.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Nfc,
                                        contentDescription = null,
                                        tint = LibraryForestGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (book.neighborhoodBorrower != null) "Lent to ${book.neighborhoodBorrower}" else "NFC Neighborhood Checkout",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen
                                )
                                Text(
                                    text = if (book.nfcTagId != null) "Tag: ${book.nfcTagId} • Tap to manage" else "Check out to a friend with an NFC tag",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = LibraryForestGreen
                        )
                    }
                }
            }

            // Status Selector Tabs
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFECE4))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BookStatus.values().forEach { status ->
                        val selected = book.status == status.name
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) Color.White else Color.Transparent,
                            shadowElevation = if (selected) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp)),
                            onClick = { onUpdateStatus(status) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = status.getDisplayName(if (book.isAudiobook) ItemType.AUDIOBOOK else ItemType.BOOK),
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) LibraryForestGreen else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Your Personal Rating (Affects AI Taste Learning & Top Rated List)
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E3D7))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "YOUR PERSONAL RATING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LibraryForestGreen,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                val filled = (book.userRating ?: 0.0) >= star
                                IconButton(
                                    onClick = { onUpdateUserRating(star.toDouble()) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (filled) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Rate $star star",
                                        tint = if (filled) LibraryGold else Color(0xFFBDBDBD),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (book.userRating != null) "You rated this ${book.userRating} ★ • AI adapts recommendations to this" else "Tap a star to rate. High ratings train the AI to recommend similar books.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Where You Left Off: Progress & Bookmark Tracking
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E3D7))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = LibraryForestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Where You Left Off",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "${book.progressPercent}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = LibraryAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { book.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = LibraryAmber,
                            trackColor = Color(0xFFEDE8DD)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (book.isAudiobook) {
                            // Audiobook progress inputs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = editAudioMinutesText,
                                    onValueChange = { editAudioMinutesText = it },
                                    label = { Text("Listened (Mins)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "/ ${book.totalDurationMinutes} mins (${book.totalDurationMinutes / 60}h ${book.totalDurationMinutes % 60}m)",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val mins = editAudioMinutesText.toIntOrNull() ?: book.listenedMinutes
                                    onUpdateAudioProgress(mins)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = LibraryForestGreen)
                            ) {
                                Text("Update Listening Progress")
                            }
                        } else {
                            // Book chapter and page inputs
                            OutlinedTextField(
                                value = editChapterText,
                                onValueChange = { editChapterText = it },
                                label = { Text("Current Chapter / Section") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = editPageText,
                                    onValueChange = { editPageText = it },
                                    label = { Text("Page Number") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "/ ${book.totalPages} pages",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val p = editPageText.toIntOrNull() ?: book.currentPage
                                    onUpdateProgress(p, editChapterText)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = LibraryForestGreen)
                            ) {
                                Text("Save Bookmark Location")
                            }
                        }
                    }
                }
            }

            // Reading Duration Tracker & Session Timer
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EFE6)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0D9CB))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = LibraryAmber
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reading Time Tracker",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "${book.readingTimeMinutes} mins logged",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LibraryForestGreen
                            )
                        }

                        val isThisTimerActive = activeTimerBookId == book.id

                        if (isThisTimerActive) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val minutes = timerSeconds / 60
                            val seconds = timerSeconds % 60
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LibraryForestGreen,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "%02d:%02d".format(minutes, seconds),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onStopTimer,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = LibraryTerracotta)
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Finish & Save Session Time")
                            }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Keep track of how long you spend reading this book in each session.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onStartTimer,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LibraryForestGreen)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Reading Timer")
                            }
                        }
                    }
                }
            }

            // Synopsis Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SYNOPSIS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = book.synopsis.ifBlank { "No synopsis available." },
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = TextPrimary
                )
            }

            // Notes Section
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR CHAPTER NOTES (${notes.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LibraryForestGreen,
                        letterSpacing = 1.sp
                    )

                    TextButton(onClick = { showAddNoteDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Note", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                if (notes.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E3D7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No notes added yet. Record your favorite quotes, chapter thoughts, or insights!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            items(notes) { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEAE5DA))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (note.chapter.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = LibraryForestGreen.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = note.chapter,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LibraryForestGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (note.pageNumber != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Page ${note.pageNumber}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteNote(note) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete note",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = note.content,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            // Delete Book Action
            item {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = {
                        onDeleteBook(book)
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Remove from Library")
                }
            }
        }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        var noteChapter by remember { mutableStateOf(book.currentChapter) }
        var notePage by remember { mutableStateOf(book.currentPage.toString()) }
        var noteText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Add Reading Note") },
            text = {
                Column {
                    OutlinedTextField(
                        value = noteChapter,
                        onValueChange = { noteChapter = it },
                        label = { Text("Chapter / Section") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notePage,
                        onValueChange = { notePage = it },
                        label = { Text("Page Number (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Your reflection or quote") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            onAddNote(noteChapter, notePage.toIntOrNull(), noteText)
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LibraryForestGreen)
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
