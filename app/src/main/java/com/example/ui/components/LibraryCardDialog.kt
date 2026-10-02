package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ai.AdaptiveTasteProfile
import com.example.data.BookEntity
import com.example.data.BookStatus
import com.example.data.UserProfileEntity
import com.example.ui.theme.*

@Composable
fun LibraryCardDialog(
    profile: UserProfileEntity?,
    books: List<BookEntity>,
    adaptiveTasteProfile: AdaptiveTasteProfile? = null,
    onViewTopRated: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        val user = profile ?: UserProfileEntity()
        val finishedBooks = books.filter { it.status == BookStatus.FINISHED.name }
        val currentlyReadingBooks = books.filter { it.status == BookStatus.CURRENTLY_READING.name }

        // Dynamic page calculation: Finished books full pages + currently reading current pages
        val totalPagesRead = finishedBooks.sumOf { it.totalPages } + currentlyReadingBooks.sumOf { it.currentPage }
        val totalAudioMinutes = books.filter { it.isAudiobook }.sumOf { it.listenedMinutes }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("library_card_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFBF8F2)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Bar with Library Card Brand
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LibraryForestGreen,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = "Library Emblem",
                                    tint = LibraryGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "METROPOLITAN LIBRARY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = LibraryForestGreen
                            )
                            Text(
                                text = "Official Reader User Card",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_library_card_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Body with Gold Foil Border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1B382B),
                                    Color(0xFF264E3D)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = LibraryGold.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        // Card Top Row: Chip & Number
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Chip simulation
                            Box(
                                modifier = Modifier
                                    .size(32.dp, 24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LibraryGold)
                            )
                            Text(
                                text = user.cardNumber,
                                color = LibraryGold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // User Name
                        Text(
                            text = if (user.username.isNotBlank()) "@${user.displayUserName.uppercase()}" else user.name.uppercase(),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "${user.name} • Member Since: ${user.memberSince} • ${user.locationCity}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Grid inside Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "PAGES READ",
                                    fontSize = 9.sp,
                                    color = LibraryGold,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "%,d".format(totalPagesRead),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = "BOOKS READ",
                                    fontSize = 9.sp,
                                    color = LibraryGold,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${finishedBooks.size}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = "AUDIO HOURS",
                                    fontSize = 9.sp,
                                    color = LibraryGold,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "%.1f h".format(totalAudioMinutes / 60f),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Currently Reading Live Bookmark Spotlight
                Text(
                    text = "WHERE YOU LEFT OFF",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (currentlyReadingBooks.isNotEmpty()) {
                    val activeBook = currentlyReadingBooks.first()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2DDD2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (activeBook.isAudiobook) Icons.Default.Headphones else Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = LibraryAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeBook.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${activeBook.currentChapter} • ${if (activeBook.isAudiobook) "${activeBook.listenedMinutes}/${activeBook.totalDurationMinutes} mins" else "Page ${activeBook.currentPage} of ${activeBook.totalPages}"}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { activeBook.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = LibraryAmber,
                                trackColor = Color(0xFFECE6DA)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${activeBook.progressPercent}% Completed • Resume reading anytime",
                                fontSize = 10.sp,
                                color = TextMuted,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2DDD2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No book currently marked in progress. Pick one from your shelves to start tracking!",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Gemini AI Learned Taste & Reading Habits
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2DDD2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
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
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI LEARNED TASTE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen,
                                    letterSpacing = 1.sp
                                )
                            }

                            if (onViewTopRated != null) {
                                Text(
                                    text = "Top Rated ★",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryAmber,
                                    modifier = Modifier.clickable { onViewTopRated() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val topG = adaptiveTasteProfile?.topGenres.orEmpty().take(3)
                        if (topG.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                topG.forEach { item ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (item.isFromRecentReads) LibraryAmber.copy(alpha = 0.15f) else Color(0xFFF3EFE6)
                                    ) {
                                        Text(
                                            text = "${item.genre} (${item.percentage}%)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (item.isFromRecentReads) LibraryAmber else TextPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = "Gemini adapts recommendations and ranks search based on your card preferences, recent reads, and ratings.",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )

                        val followed = user.getFollowedAuthorsList()
                        if (followed.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Followed: ${followed.joinToString(", ")}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF880E4F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Barcode simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(Color.White, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(2, 4, 1, 3, 2, 5, 1, 4, 3, 2, 4, 1, 3, 5, 2, 1, 4, 3, 2, 4).forEach { w ->
                            Box(
                                modifier = Modifier
                                    .width(w.dp)
                                    .fillMaxHeight()
                                    .background(Color.Black)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_card_done_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LibraryForestGreen)
                ) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
