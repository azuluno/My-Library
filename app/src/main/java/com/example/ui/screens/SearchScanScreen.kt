package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.AdaptiveTasteProfile
import com.example.ai.BookAnalysisResult
import com.example.data.BookStatus
import com.example.data.ItemType
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScanScreen(
    searchQuery: String,
    searchResults: List<BookAnalysisResult>,
    isSearching: Boolean,
    isScanning: Boolean,
    scannedResult: BookAnalysisResult?,
    adaptiveTasteProfile: AdaptiveTasteProfile? = null,
    adaptiveRecommendations: List<BookAnalysisResult> = emptyList(),
    onSearchChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onScanImage: (Bitmap) -> Unit,
    onClearScannedResult: () -> Unit,
    onAddToLibrary: (BookAnalysisResult, BookStatus, ItemType, Boolean, String?, String?) -> Unit,
    onRefreshRecommendations: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }

    // Google Play Policy compliant zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    onScanImage(bitmap)
                }
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onScanImage(bitmap)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("search_scan_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        // Screen Title
        item {
            Text(
                text = "Search & AI Book Scanner",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = LibraryForestGreen
            )
            Text(
                text = "Find books or snap a photo of any book cover to auto-pull ratings & synopsis",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Search Bar with AI Photo Button right next to it!
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("book_search_input"),
                    placeholder = { Text("Search title, author, key phrases...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LibraryForestGreen,
                        unfocusedBorderColor = Color(0xFFDCD6CA),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Mandatory AI Photo Button next to Search Bar
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = LibraryForestGreen,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showPhotoOptionsDialog = true }
                        .testTag("ai_photo_scan_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Scan Book Cover with AI",
                            tint = LibraryGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Search online / AI suggestions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onSearchSubmit(searchQuery.ifBlank { "bestselling sci-fi novels" }) },
                    modifier = Modifier.testTag("ai_auto_suggest_button")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = LibraryAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Auto-Search & Grounding", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LibraryForestGreen)
                }

                Text(
                    text = "Tip: Snap covers or type keywords",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // Scanning Loading Indicator
        if (isScanning) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LibraryGold.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = LibraryForestGreen)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Analyzing book cover with Gemini AI...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = LibraryForestGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Extracting title, author, synopsis, and Goodreads/Amazon ratings",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Scanned Book Result Card
        if (scannedResult != null && !isScanning) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scanned_book_result_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F7F1)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LibraryGold)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = LibraryForestGreen
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = LibraryGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "AI Cover Recognition Success",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = onClearScannedResult,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(width = 68.dp, height = 98.dp),
                                shadowElevation = 3.dp
                            ) {
                                AsyncImage(
                                    model = scannedResult.coverUrl.ifBlank { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80" },
                                    contentDescription = scannedResult.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = scannedResult.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "by ${scannedResult.author}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFFF7E6))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = LibraryGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "%.1f on ${scannedResult.ratingSource}".format(scannedResult.rating),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6B4500)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${scannedResult.genre} • ${scannedResult.totalPages} pages",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = scannedResult.synopsis,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Add to your personal collection:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LibraryForestGreen
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onAddToLibrary(scannedResult, BookStatus.CURRENTLY_READING, ItemType.BOOK, false, null, null)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = LibraryForestGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Reading", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    onAddToLibrary(scannedResult, BookStatus.WANT_TO_READ, ItemType.BOOK, false, null, null)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Want to Read", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    onAddToLibrary(scannedResult, BookStatus.CURRENTLY_READING, ItemType.BOOK, true, "Local Public Library", "in 14 days")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = LibraryAmber),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Checked Out", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Search Results List
        if (isSearching) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LibraryForestGreen)
                }
            }
        } else if (searchResults.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SEARCH RESULTS (${searchResults.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LibraryForestGreen,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFF3CD),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LibraryGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = LibraryAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ranked by Learned Preferences",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6D4C41)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(searchResults) { result ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E3D7))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(width = 50.dp, height = 72.dp),
                                shadowElevation = 2.dp
                            ) {
                                AsyncImage(
                                    model = result.coverUrl,
                                    contentDescription = result.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = result.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "by ${result.author}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = LibraryGold, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "%.1f (%s)".format(result.rating, result.ratingSource),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6B4500)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "${result.totalPages} pp", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = result.synopsis,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onAddToLibrary(result, BookStatus.WANT_TO_READ, ItemType.BOOK, false, null, null)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Want to Read", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onAddToLibrary(result, BookStatus.CURRENTLY_READING, ItemType.BOOK, false, null, null)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LibraryForestGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Reading", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        } else if (scannedResult == null) {
            // Adaptive Taste Discovery & Suggestions when no search query has been executed yet
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5DFD4))
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
                                    text = "GEMINI ADAPTIVE DISCOVERY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen,
                                    letterSpacing = 1.sp
                                )
                            }

                            IconButton(
                                onClick = onRefreshRecommendations,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = LibraryForestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Adapted for your reading: ${adaptiveTasteProfile?.primaryReadingFocus ?: "Eclectic Styles"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap any genre below or explore AI recommendations tailored to your recent reading shifts.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tappable genre suggestions based on learned taste
                        val learnedGenres = adaptiveTasteProfile?.topGenres.orEmpty().take(4)
                        if (learnedGenres.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                learnedGenres.forEach { item ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEFECE4),
                                        modifier = Modifier.clickable {
                                            onSearchChange(item.genre)
                                            onSearchSubmit(item.genre)
                                        }
                                    ) {
                                        Text(
                                            text = "${item.genre} (${item.percentage}%)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = LibraryForestGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (adaptiveRecommendations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Curated For You by Gemini AI:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LibraryForestGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            adaptiveRecommendations.take(3).forEach { rec ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF8F3)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEAE5DC))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.size(width = 44.dp, height = 62.dp),
                                            shadowElevation = 2.dp
                                        ) {
                                            AsyncImage(
                                                model = rec.coverUrl,
                                                contentDescription = rec.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = rec.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${rec.author} • ${rec.genre}",
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "★ ${"%.1f".format(rec.rating)} on ${rec.ratingSource}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF8B6420)
                                            )
                                        }

                                        FilledTonalButton(
                                            onClick = {
                                                onAddToLibrary(rec, BookStatus.WANT_TO_READ, ItemType.BOOK, false, null, null)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = LibraryForestGreen,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("+ Want", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

    // Photo Scanner Options Dialog
    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = LibraryForestGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Book Cover Scanner")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Take a picture of any book or upload from gallery. Gemini AI will analyze the cover to pull title, synopsis, and Goodreads/Amazon ratings.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1: Take Photo
                    OutlinedButton(
                        onClick = {
                            showPhotoOptionsDialog = false
                            cameraLauncher.launch(null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_take_photo_option"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo of Book")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 2: Upload from Device
                    OutlinedButton(
                        onClick = {
                            showPhotoOptionsDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_upload_gallery_option"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Cover from Photos")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 3: Quick Sample Cover (Guarantees immediate testing without needing physical book)
                    Button(
                        onClick = {
                            showPhotoOptionsDialog = false
                            // Create a test sample bitmap to analyze
                            val sampleBitmap = Bitmap.createBitmap(300, 450, Bitmap.Config.ARGB_8888)
                            onScanImage(sampleBitmap)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_sample_cover_option"),
                        colors = ButtonDefaults.buttonColors(containerColor = LibraryAmber),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Sample Cover (Instant Test)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoOptionsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
