package com.example.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
    onSaveProfile: (UserProfileEntity) -> Unit
) {
    val current = profile ?: UserProfileEntity()

    var username by remember(current.username) { mutableStateOf(current.username) }
    var name by remember(current.name) { mutableStateOf(current.name) }
    var email by remember(current.email) { mutableStateOf(current.email) }
    var gender by remember(current.gender) { mutableStateOf(current.gender) }
    var ageText by remember(current.age) { mutableStateOf(current.age.toString()) }
    var preferredStyles by remember(current.preferredStyles) { mutableStateOf(current.preferredStyles) }
    var bio by remember(current.bio) { mutableStateOf(current.bio) }
    var city by remember(current.locationCity) { mutableStateOf(current.locationCity) }
    var zip by remember(current.locationZip) { mutableStateOf(current.locationZip) }
    var goalMinutesText by remember(current.dailyReadingGoalMinutes) { mutableStateOf(current.dailyReadingGoalMinutes.toString()) }

    var saveFeedback by remember { mutableStateOf(false) }

    val finishedCount = books.count { it.status == BookStatus.FINISHED.name }
    val currentlyReadingCount = books.count { it.status == BookStatus.CURRENTLY_READING.name }
    val totalPagesRead = books.filter { it.status == BookStatus.FINISHED.name }.sumOf { it.totalPages } +
            books.filter { it.status == BookStatus.CURRENTLY_READING.name }.sumOf { it.currentPage }

    val genreSuggestions = listOf("Sci-Fi", "Mystery", "Fantasy", "Historical Fiction", "Non-Fiction", "Thriller", "Biography", "Literary Fiction")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        item {
            Text(
                text = "User Profile & Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = LibraryForestGreen
            )
            Text(
                text = "Customize your user name, preferences, location, and reading credentials",
                fontSize = 13.sp,
                color = TextSecondary
            )
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
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = LibraryForestGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "OFFICIAL USER CARD",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryGold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${current.displayUserName} • ${current.cardNumber}",
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "View Barcode",
                            tint = LibraryGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("PAGES READ", fontSize = 9.sp, color = LibraryGold, fontWeight = FontWeight.Bold)
                            Text("%,d".format(totalPagesRead), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("FINISHED", fontSize = 9.sp, color = LibraryGold, fontWeight = FontWeight.Bold)
                            Text("$finishedCount Books", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("READING NOW", fontSize = 9.sp, color = LibraryGold, fontWeight = FontWeight.Bold)
                            Text("$currentlyReadingCount Books", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tap anywhere on card to view full user pass",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // AI Learned Reading Taste & Top Rated card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2DDD2))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI LEARNED READING PROFILE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LibraryForestGreen,
                                letterSpacing = 1.sp
                            )
                        }

                        if (onViewTopRated != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LibraryGold.copy(alpha = 0.2f),
                                modifier = Modifier.clickable { onViewTopRated() }
                            ) {
                                Text(
                                    text = "Top Rated (${adaptiveTasteProfile?.ratedBooksCount ?: 0}) ★",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF795548),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Current Focus: ${adaptiveTasteProfile?.primaryReadingFocus ?: "Eclectic Styles"}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Gemini learns what you like as you read and rate books. High ratings and repeated styles elevate those genres on your shelves and search.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Learned Genre Affinities
                    val topG = adaptiveTasteProfile?.topGenres.orEmpty().take(4)
                    if (topG.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            topG.forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (item.isFromRecentReads) LibraryAmber.copy(alpha = 0.15f) else Color(0xFFF0EBE0)
                                ) {
                                    Text(
                                        text = "${item.genre} (${item.percentage}%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (item.isFromRecentReads) LibraryAmber else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Reading Progress Visualization feature entry card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenAnalytics() }
                    .testTag("open_analytics_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3ECE0)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2DAD0))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = LibraryForestGreen,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = LibraryGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reading Progress Visualizations",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Books read per month, speed (pp/hr) & genre chart",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // User Preferences Form
        item {
            Text(
                text = "USER ACCOUNT & PREFERENCES",
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
                    // USERNAME FIELD (Max 6 letters or numbers)
                    OutlinedTextField(
                        value = username,
                        onValueChange = { input ->
                            // Enforce max 6 letters or numbers
                            if (input.length <= 6 && input.all { it.isLetterOrDigit() }) {
                                username = input
                            }
                        },
                        label = { Text("User Name (Max 6 letters or numbers)") },
                        placeholder = { Text("e.g. alex99") },
                        supportingText = {
                            Text(
                                text = if (username.isBlank()) "Currently shown as 'User' in tabs. Add up to 6 letters/numbers." else "${username.length}/6 characters used",
                                fontSize = 11.sp,
                                color = if (username.length == 6) LibraryAmber else TextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_username_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Gender") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it },
                            label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("User Bio") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Preferred Book Styles
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
                                },
                                label = { Text(genre, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LibraryAmber,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = preferredStyles,
                        onValueChange = { preferredStyles = it },
                        label = { Text("Custom Styles (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Location & Events Area
        item {
            Text(
                text = "LOCAL AREA FOR READING EVENTS (15-MILE RADIUS)",
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Used in the Events tab to automatically surface upcoming library & book club sessions near you.",
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
                            label = { Text("City (e.g. San Diego)") },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("profile_city_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = zip,
                            onValueChange = { zip = it },
                            label = { Text("ZIP Code") },
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("profile_zip_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (saveFeedback) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = LibraryForestGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Profile & user preferences updated successfully!", color = LibraryForestGreen, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    onSaveProfile(
                        current.copy(
                            username = username.trim().take(6),
                            name = name.trim().ifBlank { "Reader" },
                            email = email.trim(),
                            gender = gender.trim(),
                            age = ageText.toIntOrNull() ?: current.age,
                            preferredStyles = preferredStyles.trim(),
                            bio = bio.trim(),
                            locationCity = city.trim().ifBlank { "San Diego" },
                            locationZip = zip.trim().ifBlank { "92101" },
                            dailyReadingGoalMinutes = goalMinutesText.toIntOrNull() ?: 30
                        )
                    )
                    saveFeedback = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_profile_button"),
                colors = ButtonDefaults.buttonColors(containerColor = LibraryForestGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Preferences & User Details")
            }
        }
    }
}
