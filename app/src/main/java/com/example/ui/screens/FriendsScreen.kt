package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.example.data.ActivityFeedItem
import com.example.data.BookEntity
import com.example.data.FriendEntity
import com.example.ui.components.SortDirection
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FriendsScreen(
    friends: List<FriendEntity>,
    userBooks: List<BookEntity>,
    activityFeed: List<ActivityFeedItem>,
    activitySortOrder: SortDirection,
    onAddFriend: (String) -> Unit,
    onLikeActivity: (String) -> Unit,
    onAddComment: (String, String) -> Unit,
    onToggleActivitySort: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Activity Feed, 1: Reading Circle & Duel
    var searchUsername by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var expandedCommentsActivityId by remember { mutableStateOf<String?>(null) }
    var commentInputText by remember { mutableStateOf("") }

    // Find shared books between the user and any friend
    val sharedComparisons = remember(friends, userBooks) {
        val comparisons = mutableListOf<SharedProgressComparison>()
        for (friend in friends) {
            val matchedUserBook = userBooks.find {
                it.title.trim().equals(friend.currentlyReadingTitle.trim(), ignoreCase = true)
            }
            if (matchedUserBook != null) {
                comparisons.add(
                    SharedProgressComparison(
                        bookTitle = matchedUserBook.title,
                        userProgress = matchedUserBook.progressPercent,
                        userChapter = matchedUserBook.currentChapter,
                        userPage = matchedUserBook.currentPage,
                        friendName = friend.displayName,
                        friendUsername = friend.username,
                        friendProgress = friend.currentlyReadingProgress,
                        friendChapter = friend.currentlyReadingChapter,
                        coverUrl = matchedUserBook.coverUrl.ifBlank { friend.currentlyReadingCoverUrl }
                    )
                )
            }
        }
        comparisons
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("friends_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        // Screen Header
        item {
            Text(
                text = "Community & Friends",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = LibraryForestGreen
            )
            Text(
                text = "Live activity feed, friend updates, and reading progress duels",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFF3ECE0),
                contentColor = LibraryForestGreen,
                indicator = {},
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Activity Feed (${activityFeed.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) LibraryForestGreen else TextSecondary
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) Color.White else Color.Transparent)
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Friends & Duel (${friends.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) LibraryForestGreen else TextSecondary
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) Color.White else Color.Transparent)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // TAB 0: ACTIVITY FEED
        if (selectedTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT FRIEND ACTIVITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LibraryForestGreen,
                        letterSpacing = 1.sp
                    )

                    // Sort By Date Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEFE8DD))
                            .clickable { onToggleActivitySort() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (activitySortOrder == SortDirection.DESCENDING) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = LibraryForestGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (activitySortOrder == SortDirection.DESCENDING) "Newest First" else "Oldest First",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LibraryForestGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(activityFeed, key = { it.id }) { item ->
                val isCommentsExpanded = expandedCommentsActivityId == item.id
                val dateString = remember(item.timestamp) {
                    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(item.timestamp))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("activity_item_${item.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E2D6)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // User Header & Activity Type
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp),
                                color = LibraryForestGreen.copy(alpha = 0.1f)
                            ) {
                                AsyncImage(
                                    model = item.friendAvatar,
                                    contentDescription = item.friendName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.friendName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "@${item.friendUsername} • $dateString",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            // Activity Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (item.actionType) {
                                    "FINISHED" -> Color(0xFFE8F5E9)
                                    "READING" -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFE3F2FD)
                                }
                            ) {
                                Text(
                                    text = when (item.actionType) {
                                        "FINISHED" -> "Finished Book"
                                        "READING" -> "Currently Reading"
                                        else -> "Want to Read"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (item.actionType) {
                                        "FINISHED" -> LibraryForestGreen
                                        "READING" -> LibraryAmber
                                        else -> Color(0xFF1976D2)
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Book Summary Content
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF9F7F2))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.size(width = 44.dp, height = 62.dp),
                                shadowElevation = 2.dp
                            ) {
                                AsyncImage(
                                    model = item.bookCover,
                                    contentDescription = item.bookTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.bookTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "by ${item.bookAuthor}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                if (item.rating != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = LibraryGold, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Rated ${item.rating} / 5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B4500))
                                    }
                                }
                                if (item.currentChapter != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${item.currentChapter} • ${item.progressPercent}% read",
                                        fontSize = 11.sp,
                                        color = LibraryAmber,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        if (item.noteSnippet != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"${item.noteSnippet}\"",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions Row: Likes & Comments
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Like Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onLikeActivity(item.id) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                        .testTag("like_button_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = if (item.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (item.isLikedByMe) Color(0xFFE53935) else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${item.likesCount}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.isLikedByMe) Color(0xFFE53935) else TextSecondary
                                    )
                                }

                                // Comment Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            expandedCommentsActivityId = if (isCommentsExpanded) null else item.id
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                        .testTag("comment_button_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = "Comment",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${item.comments.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // Expandable Comments Section
                        AnimatedVisibility(visible = isCommentsExpanded) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                HorizontalDivider(color = Color(0xFFEFE8DD))
                                Spacer(modifier = Modifier.height(8.dp))

                                if (item.comments.isEmpty()) {
                                    Text(
                                        text = "No comments yet. Be the first to share your thoughts!",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    item.comments.forEach { c ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${c.author}: ",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = LibraryForestGreen
                                            )
                                            Text(
                                                text = c.text,
                                                fontSize = 12.sp,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Comment Input Field
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = commentInputText,
                                        onValueChange = { commentInputText = it },
                                        placeholder = { Text("Write a comment...", fontSize = 12.sp) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("comment_input_${item.id}"),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (commentInputText.isNotBlank()) {
                                                onAddComment(item.id, commentInputText)
                                                commentInputText = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .background(LibraryForestGreen, CircleShape)
                                            .size(40.dp)
                                            .testTag("post_comment_button_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Post comment",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: FRIENDS & PROGRESS DUEL
        if (selectedTab == 1) {
            // Friend lookup & add bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchUsername,
                        onValueChange = { searchUsername = it },
                        placeholder = { Text("Look up @username (e.g. sarah_reads)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = null, tint = TextSecondary)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("friend_search_input"),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LibraryForestGreen,
                            unfocusedBorderColor = Color(0xFFDCD6CA),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (searchUsername.isNotBlank()) {
                                onAddFriend(searchUsername)
                                snackbarMessage = "Added @${searchUsername.removePrefix("@")} as reading friend!"
                                searchUsername = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LibraryForestGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("add_friend_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = LibraryGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add")
                    }
                }

                if (snackbarMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LibraryForestGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(snackbarMessage!!, color = LibraryForestGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Shared Book Progress Duel / Comparison Section
            if (sharedComparisons.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            tint = LibraryAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SHARED BOOK READING DUEL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LibraryForestGreen,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You and your friends are reading the same books! Here is your side-by-side progress:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(sharedComparisons) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("comparison_card_${item.bookTitle}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, LibraryGold.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.size(width = 38.dp, height = 54.dp),
                                    shadowElevation = 2.dp
                                ) {
                                    AsyncImage(
                                        model = item.coverUrl,
                                        contentDescription = item.bookTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.bookTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = TextPrimary
                                    )
                                    val diff = item.userProgress - item.friendProgress
                                    val statusText = when {
                                        diff > 0 -> "You are leading by $diff%!"
                                        diff < 0 -> "${item.friendName} is leading by ${-diff}%!"
                                        else -> "You are tied neck-and-neck!"
                                    }
                                    Text(
                                        text = statusText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (diff >= 0) LibraryForestGreen else LibraryTerracotta
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // You bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("You (${item.userChapter})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LibraryForestGreen)
                                Text("${item.userProgress}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LibraryForestGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { item.userProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = LibraryForestGreen,
                                trackColor = Color(0xFFE8E4DA)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Friend bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${item.friendName} (@${item.friendUsername}) • ${item.friendChapter}", fontSize = 12.sp, color = LibraryAmber, fontWeight = FontWeight.SemiBold)
                                Text("${item.friendProgress}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LibraryAmber)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { item.friendProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = LibraryAmber,
                                trackColor = Color(0xFFE8E4DA)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Friends Shelf List
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = LibraryForestGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "YOUR READING CIRCLE (${friends.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LibraryForestGreen,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(friends, key = { it.id }) { friend ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .testTag("friend_card_${friend.username}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEAE5DA))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                modifier = Modifier.size(46.dp),
                                color = LibraryForestGreen.copy(alpha = 0.1f)
                            ) {
                                if (friend.avatarUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = friend.avatarUrl,
                                        contentDescription = friend.displayName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = friend.displayName.take(1),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LibraryForestGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = friend.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "@${friend.username} • ${friend.booksFinishedCount} books finished",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = "Friend",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (friend.currentlyReadingTitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF9F7F2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E3D7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = LibraryAmber, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Reading: ${friend.currentlyReadingTitle}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${friend.currentlyReadingChapter} • ${friend.currentlyReadingProgress}% complete",
                                            fontSize = 11.sp,
                                            color = TextSecondary
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

data class SharedProgressComparison(
    val bookTitle: String,
    val userProgress: Int,
    val userChapter: String,
    val userPage: Int,
    val friendName: String,
    val friendUsername: String,
    val friendProgress: Int,
    val friendChapter: String,
    val coverUrl: String
)
