package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.NeighborhoodCheckoutEntity
import com.example.ui.theme.LibraryAmber
import com.example.ui.theme.LibraryForestGreen
import com.example.ui.theme.LibraryGold
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsAlertsSheet(
    checkouts: List<NeighborhoodCheckoutEntity>,
    onDismiss: () -> Unit,
    onOpenMessage: (NeighborhoodCheckoutEntity) -> Unit,
    onMarkReturned: (Long) -> Unit,
    onNewNfcCheckout: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val activeCheckouts = remember(checkouts) { checkouts.filter { !it.isReturned } }
    val returnedCheckouts = remember(checkouts) { checkouts.filter { it.isReturned } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("notifications_alerts_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = LibraryForestGreen.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Notifications",
                                tint = LibraryForestGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Notifications & Alerts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LibraryForestGreen
                        )
                        Text(
                            text = "${activeCheckouts.size} active neighborhood book alerts",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onNewNfcCheckout,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = LibraryForestGreen,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("NFC Loan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (activeCheckouts.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF9F7F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFECE7DE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = LibraryAmber,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "All Caught Up!",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen
                                )
                                Text(
                                    text = "No upcoming return dates or pending book alerts right now.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "UPCOMING RETURN DATES & ALERTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LibraryForestGreen,
                            letterSpacing = 1.sp
                        )
                    }

                    items(activeCheckouts) { checkout ->
                        val isLent = checkout.isLentToFriend
                        val remainingDays = checkout.daysRemaining
                        val isOverdue = checkout.isOverdue
                        val isDueSoon = checkout.isDueSoon

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isOverdue -> Color(0xFFFFF0F0)
                                    isDueSoon -> Color(0xFFFFF9E6)
                                    else -> Color(0xFFFAF8F5)
                                }
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when {
                                    isOverdue -> Color(0xFFFFCDD2)
                                    isDueSoon -> Color(0xFFFFE082)
                                    else -> Color(0xFFE6E1D8)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Top Alert Banner Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = if (isOverdue) Color(0xFFD32F2F) else LibraryAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isLent) {
                                                if (isOverdue) "Time for ${checkout.personName} to return book!"
                                                else if (isDueSoon) "Due soon: ${checkout.personName} returning in $remainingDays days"
                                                else "Lent to ${checkout.personName}"
                                            } else {
                                                if (isOverdue) "Your return date is overdue! Return to ${checkout.personName}"
                                                else "Your return date is coming up!"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOverdue) Color(0xFFD32F2F) else Color(0xFF5D4037)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isOverdue) Color(0xFFFFCDD2) else if (isDueSoon) Color(0xFFFFE0A3) else Color(0xFFE3F2FD)
                                    ) {
                                        Text(
                                            text = when {
                                                isOverdue -> "OVERDUE"
                                                remainingDays == 0 -> "TODAY"
                                                remainingDays == 1 -> "TOMORROW"
                                                else -> "$remainingDays DAYS"
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOverdue) Color(0xFFB71C1C) else if (isDueSoon) Color(0xFF6B4500) else Color(0xFF1565C0),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Book info row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(width = 46.dp, height = 62.dp),
                                        shadowElevation = 1.dp
                                    ) {
                                        if (checkout.bookCoverUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = checkout.bookCoverUrl,
                                                contentDescription = checkout.bookTitle,
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
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = checkout.bookTitle,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2C251D),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "by ${checkout.bookAuthor}",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Nfc,
                                                contentDescription = null,
                                                tint = LibraryForestGreen,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${checkout.nfcTagId} • Return by ${dateFormat.format(Date(checkout.dueDateMillis))}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF4A4438)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Action Buttons: Message In-App and Mark Returned
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onOpenMessage(checkout) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LibraryForestGreen),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ChatBubbleOutline,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Message ${checkout.personName}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { onMarkReturned(checkout.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = LibraryForestGreen,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Mark Returned", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // History of completed checkouts
                if (returnedCheckouts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "COMPLETED CHECKOUTS (${returnedCheckouts.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }

                    items(returnedCheckouts) { past ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF7F5F0),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = past.bookTitle,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4A4438)
                                    )
                                    Text(
                                        text = "${if (past.isLentToFriend) "Lent to" else "Borrowed from"} ${past.personName} • Returned",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                TextButton(onClick = { onOpenMessage(past) }) {
                                    Text("Messages", fontSize = 11.sp, color = LibraryForestGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
