package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeLoginDialog(
    initialUsername: String = "",
    initialName: String = "Morgan Reed",
    initialEmail: String = "",
    onSaveAccount: (username: String, name: String, email: String, preferredGenres: String, followedAuthors: String) -> Unit,
    onContinueAsGuest: () -> Unit
) {
    // Mode: false = compact msg-box style popup, true = full card signup view
    var isFullCardView by remember { mutableStateOf(false) }

    var usernameInput by remember { mutableStateOf(initialUsername) }
    var nameInput by remember { mutableStateOf(initialName) }
    var emailInput by remember { mutableStateOf(initialEmail) }

    var customAuthorInput by remember { mutableStateOf("") }
    var followedAuthorsList by remember { mutableStateOf(listOf<String>()) }

    val genreOptions = listOf("Sci-Fi", "Mystery", "Fantasy", "Non-Fiction", "Thriller", "Romance")
    var selectedGenres by remember { mutableStateOf(setOf("Sci-Fi", "Mystery")) }

    Dialog(
        onDismissRequest = onContinueAsGuest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedContent(
            targetState = isFullCardView,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "welcome_dialog_anim"
        ) { fullView ->
            if (!fullView) {
                // ==========================================
                // COMPACT MESSAGE-BOX STYLE WELCOME POPUP
                // ==========================================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .wrapContentHeight()
                        .padding(16.dp)
                        .testTag("welcome_login_dialog"),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    shadowElevation = 14.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2DDD2))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Original App Icon: Gold Book with Bookmark
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFBF6EB),
                            border = androidx.compose.foundation.BorderStroke(2.dp, LibraryGold),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(62.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = "My Library App",
                                    tint = LibraryGold,
                                    modifier = Modifier.size(32.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = LibraryAmber,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(top = 10.dp, end = 12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // App Name: My Library
                        Text(
                            text = "Welcome to My Library",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LibraryForestGreen,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Set up your reader account to personalize your official patron library card, favorite authors, and reading journey. Or explore as guest.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Primary Action: Sign Up / Set Up Account -> Opens full card view
                        Button(
                            onClick = { isFullCardView = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("welcome_signup_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LibraryForestGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign Up / Set Up Account",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Secondary Action: Browse as Guest
                        OutlinedButton(
                            onClick = onContinueAsGuest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("welcome_guest_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A4438)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8D2C5))
                        ) {
                            Text(
                                text = "Browse as Guest",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4A4438)
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // FULL CARD SIGNUP VIEW
                // ==========================================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .fillMaxHeight(0.88f)
                        .padding(vertical = 12.dp)
                        .testTag("welcome_full_card_view"),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 16.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2DDD2))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Bar: Back arrow + Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { isFullCardView = false }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LibraryForestGreen)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Set Up Your Patron Account",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = LibraryForestGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Official Library Card Preview
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = LibraryForestGreen),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = LibraryGold,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                                    contentDescription = null,
                                                    tint = LibraryForestGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "MY LIBRARY PATRON CARD",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = LibraryGold,
                                                letterSpacing = 1.sp
                                            )
                                            Text(
                                                text = if (usernameInput.isNotBlank()) usernameInput else "New Patron",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "PATRON",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("NAME", fontSize = 8.sp, color = LibraryGold)
                                        Text(if (nameInput.isNotBlank()) nameInput else "Morgan Reed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Column {
                                        Text("CARD NO.", fontSize = 8.sp, color = LibraryGold)
                                        Text("LIB-7842-CA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Column {
                                        Text("STATUS", fontSize = 8.sp, color = LibraryGold)
                                        Text("ACTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Username field
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { input ->
                                val clean = input.filter { it.isLetterOrDigit() || it == '_' }.take(10)
                                usernameInput = clean
                            },
                            label = { Text("Choose a Username *") },
                            placeholder = { Text("e.g. alex99, bookworm") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = LibraryForestGreen)
                            },
                            supportingText = {
                                Text(
                                    text = if (usernameInput.isBlank()) "Will appear on your patron card and tabs (up to 10 chars)" else "${usernameInput.length}/10 characters",
                                    fontSize = 11.sp,
                                    color = if (usernameInput.length == 10) LibraryAmber else TextMuted
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("welcome_username_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full Name field
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Morgan Reed") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = LibraryForestGreen)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("welcome_name_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Email field (Optional)
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email (Optional)") },
                            placeholder = { Text("reader@example.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("welcome_email_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ==========================================
                        // FOLLOW AN AUTHOR: DEDICATED INPUT TEXT AREA
                        // (So user can add author directly, without random forced authors)
                        // ==========================================
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "FOLLOW AN AUTHOR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LibraryForestGreen,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Type an author you like to follow their books (e.g. Andy Weir, Stephen King):",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customAuthorInput,
                                    onValueChange = { customAuthorInput = it },
                                    placeholder = { Text("Enter author name...") },
                                    leadingIcon = {
                                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color(0xFFE91E63))
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("welcome_author_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (customAuthorInput.isNotBlank()) {
                                            val clean = customAuthorInput.trim()
                                            if (!followedAuthorsList.any { it.equals(clean, ignoreCase = true) }) {
                                                followedAuthorsList = followedAuthorsList + clean
                                            }
                                            customAuthorInput = ""
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

                            // Followed Author Chips with delete button
                            if (followedAuthorsList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    followedAuthorsList.forEach { author ->
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color(0xFFFFEBEE),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Favorite,
                                                    contentDescription = null,
                                                    tint = Color(0xFFE91E63),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = author,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFC2185B)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove $author",
                                                    tint = Color(0xFFC2185B),
                                                    modifier = Modifier
                                                        .size(13.dp)
                                                        .clickable {
                                                            followedAuthorsList = followedAuthorsList.filterNot { it.equals(author, ignoreCase = true) }
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Favorite Genres
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "PREFERRED BOOK GENRES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LibraryForestGreen,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                genreOptions.forEach { genre ->
                                    val isSelected = selectedGenres.contains(genre)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedGenres = if (isSelected) {
                                                selectedGenres - genre
                                            } else {
                                                selectedGenres + genre
                                            }
                                        },
                                        label = { Text(genre, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LibraryForestGreen,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Primary Save Account Button (with guaranteed visible white text)
                        Button(
                            onClick = {
                                val finalUsername = usernameInput.trim().ifBlank {
                                    nameInput.trim().filter { it.isLetterOrDigit() }.take(8).lowercase().ifBlank { "reader1" }
                                }
                                onSaveAccount(
                                    finalUsername,
                                    nameInput.trim().ifBlank { "Morgan Reed" },
                                    emailInput.trim(),
                                    selectedGenres.joinToString(", "),
                                    followedAuthorsList.joinToString(", ")
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("welcome_save_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LibraryForestGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save & Enter Library",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Guest / Skip Option
                        OutlinedButton(
                            onClick = onContinueAsGuest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("welcome_guest_button_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF555047)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8D2C5))
                        ) {
                            Text(
                                text = "Continue as Guest",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF555047)
                            )
                        }
                    }
                }
            }
        }
    }
}
