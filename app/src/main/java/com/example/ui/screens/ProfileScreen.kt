package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AdaptiveTasteProfile
import com.example.data.BookEntity
import com.example.data.BookStatus
import com.example.data.UserProfileEntity
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    profile: UserProfileEntity?,
    books: List<BookEntity>,
    adaptiveTasteProfile: AdaptiveTasteProfile? = null,
    onViewTopRated: (() -> Unit)? = null,
    onOpenLibraryCard: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onSaveProfile: (UserProfileEntity) -> Unit,
    onClearAllMessages: (() -> Unit)? = null,
    onOpenWelcomeLogin: (() -> Unit)? = null
) {
    val current = profile ?: UserProfileEntity()

    // Decoupled remember state so typing is never interrupted or wiped out by external recompositions
    var username by remember { mutableStateOf(current.username) }
    var name by remember { mutableStateOf(current.name) }
    var email by remember { mutableStateOf(current.email) }
    var gender by remember { mutableStateOf(current.gender) }
    var ageText by remember { mutableStateOf(current.age.toString()) }
    var preferredStyles by remember { mutableStateOf(current.preferredStyles) }
    var followedAuthorsText by remember { mutableStateOf(current.followedAuthors) }
    var autoDeleteMessages by remember { mutableStateOf(current.autoDeleteMessages) }
    var bio by remember { mutableStateOf(current.bio) }
    var city by remember { mutableStateOf(current.locationCity) }
    var zip by remember { mutableStateOf(current.locationZip) }
    var goalMinutesText by remember { mutableStateOf(current.dailyReadingGoalMinutes.toString()) }

    var newAuthorInput by remember { mutableStateOf("") }
    var saveFeedback by remember { mutableStateOf(false) }

    // Synchronize initial state when profile is first loaded from database if local fields are blank
    LaunchedEffect(profile) {
        if (profile != null) {
            if (username.isBlank() && profile.username.isNotBlank()) username = profile.username
            if (name.isBlank() && profile.name.isNotBlank()) name = profile.name
            if (email.isBlank() && profile.email.isNotBlank()) email = profile.email
            if (city.isBlank() && profile.locationCity.isNotBlank()) city = profile.locationCity
            if (zip.isBlank() && profile.locationZip.isNotBlank()) zip = profile.locationZip
            if (followedAuthorsText.isBlank() && profile.followedAuthors.isNotBlank()) followedAuthorsText = profile.followedAuthors
        }
    }

    val genreSuggestions = listOf("Sci-Fi", "Mystery", "Fantasy", "Historical Fiction", "Non-Fiction", "Thriller", "Biography", "Romance")
    val authorSuggestions = listOf("Emily Henry", "Andy Weir", "Stephen King", "Agatha Christie", "Brandon Sanderson", "Neil Gaiman")

    val followedAuthorsList = remember(followedAuthorsText) {
        followedAuthorsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    fun performSave() {
        val updated = current.copy(
            username = username.trim().filter { it.isLetterOrDigit() || it == '_' }.take(10),
            name = name.trim().ifBlank { "Morgan Reed" },
            email = email.trim(),
            gender = gender.trim().ifBlank { "Reader" },
            age = ageText.toIntOrNull() ?: current.age,
            preferredStyles = preferredStyles.trim(),
            followedAuthors = followedAuthorsText.trim(),
            autoDeleteMessages = autoDeleteMessages,
            bio = bio.trim(),
            locationCity = city.trim().ifBlank { "San Diego" },
            locationZip = zip.trim().ifBlank { "92101" },
            dailyReadingGoalMinutes = goalMinutesText.toIntOrNull() ?: 30,
            hasCompletedWelcome = true,
            isGuest = username.isBlank()
        )
        onSaveProfile(updated)
        saveFeedback = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header & Quick Save Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "User Profile & Account",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = LibraryForestGreen
                    )
                    Text(
                        text = "Customize username, followed authors & preferences",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                // Quick Top Save Button (Always visible without scrolling!)
                Button(
                    onClick = { performSave() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LibraryForestGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("top_save_profile_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            AnimatedVisibility(
                visible = saveFeedback,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LibraryForestGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Changes saved to your phone successfully!",
                            color = LibraryForestGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tap-to-view Official User Library Card Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onOpenLibraryCard() }
                    .testTag("open_library_card_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LibraryForestGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = LibraryGold,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = LibraryForestGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "OFFICIAL LIBRARY PATRON CARD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryGold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (username.isNotBlank()) username else (if (current.username.isNotBlank()) current.username else "Guest Reader"),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "TAP TO VIEW",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("CARD NO.", fontSize = 9.sp, color = LibraryGold)
                            Text(current.cardNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("ACCOUNT TYPE", fontSize = 9.sp, color = LibraryGold)
                            Text(
                                if (username.isNotBlank()) "SAVED PROFILE" else "GUEST USER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text("CITY", fontSize = 9.sp, color = LibraryGold)
                            Text(if (city.isNotBlank()) city else "San Diego", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section 1: User Account & Identity (Fully Functional & Interactive)
        item {
            Text(
                text = "USER ACCOUNT & IDENTITY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LibraryForestGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E3D7))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // USERNAME FIELD
                    OutlinedTextField(
                        value = username,
                        onValueChange = { input ->
                            // Allow typing letters, numbers, and underscores up to 10 chars
                            val clean = input.filter { it.isLetterOrDigit() || it == '_' }.take(10)
                            username = clean
                        },
                        label = { Text("User Name (Saved on your phone)") },
                        placeholder = { Text("e.g. alex99, bookworm") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = LibraryForestGreen)
                        },
                        trailingIcon = {
                            if (username.isNotBlank()) {
                                IconButton(onClick = { username = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        supportingText = {
                            Text(
                                text = if (username.isBlank()) "Appears on bottom tabs, patron card & lending. Leave blank for 'User'." else "${username.length}/10 characters",
                                fontSize = 11.sp,
                                color = if (username.length >= 10) LibraryAmber else TextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_username_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // FULL NAME FIELD
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. Morgan Reed") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = LibraryForestGreen)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // EMAIL FIELD
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        placeholder = { Text("reader@example.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_email_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // GENDER & AGE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Reader Type / Gender") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it.filter { char -> char.isDigit() }.take(3) },
                            label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // USER BIO
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Reader Bio") },
                        placeholder = { Text("What kinds of stories do you love reading?") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Switch / Reset Account Button
                    if (onOpenWelcomeLogin != null) {
                        OutlinedButton(
                            onClick = onOpenWelcomeLogin,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Re-open Welcome Login Setup Dialog", fontSize = 12.sp)
                        }
                    }

                    // Direct section save button
                    Button(
                        onClick = { performSave() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LibraryForestGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Account Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section 2: Followed Authors (Heart & Follow System)
        item {
            Text(
                text = "FOLLOWED AUTHORS (FAVORITES)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LibraryForestGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Authors you follow will populate up more in your library and search. You can also heart authors directly on books!",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E3D7))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Currently followed authors chips with delete button
                    if (followedAuthorsList.isNotEmpty()) {
                        Text(
                            text = "Currently Following (${followedAuthorsList.size}):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LibraryForestGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            followedAuthorsList.forEach { author ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFFFEBEE),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = Color(0xFFE91E63),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = author,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC2185B)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Unfollow $author",
                                            tint = Color(0xFFC2185B),
                                            modifier = Modifier
                                                .size(15.dp)
                                                .clickable {
                                                    val updated = followedAuthorsList.filterNot { it.equals(author, ignoreCase = true) }
                                                    followedAuthorsText = updated.joinToString(", ")
                                                    performSave()
                                                }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Add Custom Author
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newAuthorInput,
                            onValueChange = { newAuthorInput = it },
                            placeholder = { Text("Follow author (e.g. Neil Gaiman)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_author_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newAuthorInput.isNotBlank()) {
                                    val clean = newAuthorInput.trim()
                                    if (!followedAuthorsList.any { it.equals(clean, ignoreCase = true) }) {
                                        val updated = followedAuthorsList + clean
                                        followedAuthorsText = updated.joinToString(", ")
                                        performSave()
                                    }
                                    newAuthorInput = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LibraryForestGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text("+ Follow", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Suggested Authors
                    Text(
                        text = "Suggested Authors to Follow:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        authorSuggestions.forEach { author ->
                            val isFollowing = followedAuthorsList.any { it.equals(author, ignoreCase = true) }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isFollowing) Color(0xFFFFEBEE) else Color(0xFFF1EFEA),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isFollowing) Color(0xFFFFCDD2) else Color.Transparent),
                                modifier = Modifier.clickable {
                                    val updated = if (isFollowing) {
                                        followedAuthorsList.filterNot { it.equals(author, ignoreCase = true) }
                                    } else {
                                        followedAuthorsList + author
                                    }
                                    followedAuthorsText = updated.joinToString(", ")
                                    performSave()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isFollowing) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (isFollowing) Color(0xFFE91E63) else Color.Gray,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = author,
                                        fontSize = 11.sp,
                                        color = if (isFollowing) Color(0xFFC2185B) else Color(0xFF4A4438),
                                        fontWeight = if (isFollowing) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section 3: Preferred Book Styles & Genres
        item {
            Text(
                text = "PREFERRED BOOK STYLES & GENRES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LibraryForestGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E3D7))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tap genres to add or remove from your reading profile:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        genreSuggestions.forEach { genre ->
                            val isSelected = preferredStyles.contains(genre, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val currentList = preferredStyles.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
                                    if (isSelected) {
                                        currentList.removeAll { it.equals(genre, ignoreCase = true) }
                                    } else {
                                        currentList.add(genre)
                                    }
                                    preferredStyles = currentList.joinToString(", ")
                                    performSave()
                                },
                                label = { Text(genre, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LibraryForestGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = preferredStyles,
                        onValueChange = { preferredStyles = it },
                        label = { Text("Custom Styles (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section 4: Neighborhood Lending & NFC Message Preferences
        item {
            Text(
                text = "NEIGHBORHOOD LENDING & IN-APP MESSAGES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LibraryForestGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Delete Returned Messages",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Automatically remove in-app lending messages when a book is marked returned.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = autoDeleteMessages,
                            onCheckedChange = {
                                autoDeleteMessages = it
                                performSave()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = LibraryForestGreen),
                            modifier = Modifier.testTag("auto_delete_messages_switch")
                        )
                    }

                    if (onClearAllMessages != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF0EBE0))
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onClearAllMessages,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear All In-App Messages Now", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section 5: Location & Reading Goals
        item {
            Text(
                text = "LOCAL AREA & READING GOALS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LibraryForestGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E3D7))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Used in the Events tab to surface library sessions and book club events near you.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("profile_city_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = zip,
                            onValueChange = { zip = it },
                            label = { Text("ZIP Code") },
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("profile_zip_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = goalMinutesText,
                        onValueChange = { goalMinutesText = it.filter { c -> c.isDigit() } },
                        label = { Text("Daily Reading Goal (Minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large bottom save button
            Button(
                onClick = { performSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LibraryForestGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save All Preferences & Details", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
