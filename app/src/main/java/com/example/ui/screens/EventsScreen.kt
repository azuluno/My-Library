package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventEntity
import com.example.data.UserProfileEntity
import com.example.ui.theme.*

@Composable
fun EventsScreen(
    events: List<EventEntity>,
    userProfile: UserProfileEntity?,
    isRefreshing: Boolean,
    onToggleSave: (eventId: Long, isSaved: Boolean) -> Unit,
    onRefreshWithAi: (city: String, zip: String) -> Unit,
    onNavigateToPreferences: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    val city = userProfile?.locationCity?.ifBlank { "San Diego" } ?: "San Diego"
    val zip = userProfile?.locationZip?.ifBlank { "92101" } ?: "92101"

    val categories = listOf("ALL", "Book Club", "Author Signing", "Reading Meetup", "Book Sale", "Workshop")

    val filteredEvents = events.filter { event ->
        if (selectedCategory == "ALL") true else event.category.contains(selectedCategory, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("events_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Upcoming Literary Events",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = LibraryForestGreen
                    )
                    Text(
                        text = "Book clubs, library sessions & author signings",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Area indicator card (Within 15-mile radius)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPreferences() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF2ECE1)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2DAD0))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = LibraryTerracotta,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Area: $city (ZIP $zip)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Showing events within 15-mile radius • Tap to edit location",
                            fontSize = 11.sp,
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

            Spacer(modifier = Modifier.height(12.dp))

            // Refresh events with AI Search Grounding
            Button(
                onClick = { onRefreshWithAi(city, zip) },
                enabled = !isRefreshing,
                colors = ButtonDefaults.buttonColors(containerColor = LibraryForestGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("refresh_events_button")
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Searching live events for $city...")
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = LibraryGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Find Local Events with AI Search Grounding")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LibraryForestGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Event List: "populate a list with the address or location under the name of the event"
        if (filteredEvents.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No events found in this category", fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tap 'Find Local Events with AI' to search real events for your area.", fontSize = 12.sp, color = TextMuted)
                }
            }
        } else {
            items(filteredEvents, key = { it.id }) { event ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("event_item_${event.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9E4D8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = LibraryForestGreen.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = event.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LibraryForestGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Event Name
                                Text(
                                    text = event.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // MANDATORY REQUIREMENT: Address / Location directly under the event name
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = LibraryTerracotta,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${event.locationName}, ${event.address} (${event.city}, CA ${event.zipCode})",
                                        fontSize = 12.sp,
                                        color = LibraryTerracotta,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onToggleSave(event.id, !event.isSaved) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (event.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Save event",
                                    tint = if (event.isSaved) LibraryAmber else TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Date and Time Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = event.date, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = event.time, fontSize = 12.sp, color = TextSecondary)
                            }

                            Text(
                                text = "• ${event.attendeeCount} attending",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = event.description,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
