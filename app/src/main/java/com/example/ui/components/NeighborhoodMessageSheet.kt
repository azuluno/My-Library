package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NeighborhoodCheckoutEntity
import com.example.data.NeighborhoodMessageEntity
import com.example.ui.theme.LibraryAmber
import com.example.ui.theme.LibraryForestGreen
import com.example.ui.theme.LibraryGold
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeighborhoodMessageSheet(
    checkout: NeighborhoodCheckoutEntity,
    messages: List<NeighborhoodMessageEntity>,
    myUsername: String,
    onSendMessage: (text: String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val timeFormat = remember { SimpleDateFormat("h:mm a • MMM d", Locale.getDefault()) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("neighborhood_message_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(bottom = 16.dp)
        ) {
            // Header: Person Name, Book Title & Due status
            Surface(
                color = Color(0xFFF9F7F2),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = LibraryForestGreen,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = checkout.personName.firstOrNull()?.uppercase() ?: "N",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = checkout.personName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LibraryForestGreen
                            )
                            Text(
                                text = "Re: ${checkout.bookTitle}",
                                fontSize = 12.sp,
                                color = Color(0xFF6B4500),
                                fontWeight = FontWeight.Medium
                            )
                            val remaining = checkout.daysRemaining
                            val dueText = when {
                                checkout.isReturned -> "Returned"
                                remaining < 0 -> "Overdue by ${-remaining} days"
                                remaining == 0 -> "Due Today"
                                else -> "Due in $remaining days"
                            }
                            Text(
                                text = "📅 $dueText • In-App Direct Message",
                                fontSize = 10.sp,
                                color = if (remaining < 0) Color.Red else Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFECE7DE))

            // Quick suggested replies
            val isLent = checkout.isLentToFriend
            val suggestions = if (isLent) {
                listOf(
                    "Hey ${checkout.personName}! How are you enjoying the book?",
                    "Return date is coming up in ${checkout.daysRemaining} days!",
                    "Let me know if you need an extension to finish reading!"
                )
            } else {
                listOf(
                    "Thanks for checking this out to me!",
                    "Almost finished! Ready to return it soon.",
                    "Would it be okay if I keep it a few more days?"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.take(2).forEach { suggestion ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF3EFE6),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { inputText = suggestion }
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 10.sp,
                            maxLines = 2,
                            color = Color(0xFF4A4438),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No messages yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "Send a message to ${checkout.personName} about this checkout.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                items(messages) { msg ->
                    val isMe = msg.isFromMe
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            color = if (isMe) LibraryForestGreen else Color(0xFFEFECE4),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (!isMe) {
                                    Text(
                                        text = msg.senderName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LibraryAmber
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = if (isMe) Color.White else Color(0xFF2C251D)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = timeFormat.format(Date(msg.timestampMillis)),
                                    fontSize = 9.sp,
                                    color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = Color.White,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Message ${checkout.personName}...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("in_app_message_input"),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(LibraryForestGreen)
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
