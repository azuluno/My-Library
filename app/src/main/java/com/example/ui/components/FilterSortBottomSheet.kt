package com.example.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookEntity
import com.example.ui.theme.LibraryForestGreen
import com.example.ui.theme.LibraryGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class SortField(val displayName: String) {
    TITLE("Title"),
    AUTHOR("Author"),
    RATING("Rating"),
    DATE_ADDED("Date Added")
}

enum class SortDirection {
    ASCENDING,
    DESCENDING
}

data class FilterSortState(
    val selectedGenre: String? = null,
    val selectedAuthor: String? = null,
    val selectedPublicationEra: String? = null, // "All", "Classic (Pre-2000)", "Modern (2000-2020)", "Recent (2021+)"
    val minRating: Double? = null,
    val selectedStatus: String? = null, // null, "CURRENTLY_READING", "WANT_TO_READ", "FINISHED", "CHECKED_OUT"
    val sortField: SortField = SortField.DATE_ADDED,
    val sortDirection: SortDirection = SortDirection.DESCENDING
) {
    val activeFilterCount: Int
        get() = (if (selectedGenre != null) 1 else 0) +
                (if (selectedAuthor != null) 1 else 0) +
                (if (selectedPublicationEra != null) 1 else 0) +
                (if (minRating != null) 1 else 0) +
                (if (selectedStatus != null) 1 else 0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortBottomSheet(
    currentState: FilterSortState,
    availableBooks: List<BookEntity>,
    onApply: (FilterSortState) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var state by remember { mutableStateOf(currentState) }

    val allGenres = remember(availableBooks) {
        availableBooks.map { it.genre.split("/").first().trim() }.distinct().filter { it.isNotBlank() }
    }
    val allAuthors = remember(availableBooks) {
        availableBooks.map { it.author.trim() }.distinct().filter { it.isNotBlank() }
    }

    val eraOptions = listOf("Classic (Pre-2000)", "Modern (2000-2020)", "Recent (2021+)")
    val ratingOptions = listOf(4.0, 4.5, 4.8)
    val statusOptions = listOf(
        "CURRENTLY_READING" to "Currently Reading",
        "WANT_TO_READ" to "Want to Read",
        "FINISHED" to "Finished",
        "CHECKED_OUT" to "Checked Out"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFBF9F5),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("filter_sort_bottom_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = LibraryForestGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Filter & Sort Books",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LibraryForestGreen
                        )
                    }

                    TextButton(onClick = {
                        state = FilterSortState()
                        onReset()
                    }) {
                        Text("Reset All", color = Color(0xFFB8532F), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 1. Sort By
            item {
                Text(
                    text = "SORT BY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SortField.values()) { field ->
                        FilterChip(
                            selected = state.sortField == field,
                            onClick = {
                                if (state.sortField == field) {
                                    // toggle direction
                                    state = state.copy(
                                        sortDirection = if (state.sortDirection == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
                                    )
                                } else {
                                    state = state.copy(sortField = field)
                                }
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(field.displayName, fontSize = 12.sp)
                                    if (state.sortField == field) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = if (state.sortDirection == SortDirection.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LibraryForestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. Reading Status Filter
            item {
                Text(
                    text = "READING STATUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedStatus == null,
                            onClick = { state = state.copy(selectedStatus = null) },
                            label = { Text("All Statuses", fontSize = 12.sp) }
                        )
                    }
                    items(statusOptions) { (key, label) ->
                        FilterChip(
                            selected = state.selectedStatus == key,
                            onClick = {
                                state = state.copy(selectedStatus = if (state.selectedStatus == key) null else key)
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LibraryForestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Genre Filter
            item {
                Text(
                    text = "GENRE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedGenre == null,
                            onClick = { state = state.copy(selectedGenre = null) },
                            label = { Text("All Genres", fontSize = 12.sp) }
                        )
                    }
                    items(allGenres) { genre ->
                        FilterChip(
                            selected = state.selectedGenre == genre,
                            onClick = {
                                state = state.copy(selectedGenre = if (state.selectedGenre == genre) null else genre)
                            },
                            label = { Text(genre, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LibraryForestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 4. Author Filter
            item {
                Text(
                    text = "AUTHOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedAuthor == null,
                            onClick = { state = state.copy(selectedAuthor = null) },
                            label = { Text("All Authors", fontSize = 12.sp) }
                        )
                    }
                    items(allAuthors) { author ->
                        FilterChip(
                            selected = state.selectedAuthor == author,
                            onClick = {
                                state = state.copy(selectedAuthor = if (state.selectedAuthor == author) null else author)
                            },
                            label = { Text(author, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LibraryForestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 5. Publication Date Era
            item {
                Text(
                    text = "PUBLICATION ERA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedPublicationEra == null,
                            onClick = { state = state.copy(selectedPublicationEra = null) },
                            label = { Text("Any Era", fontSize = 12.sp) }
                        )
                    }
                    items(eraOptions) { era ->
                        FilterChip(
                            selected = state.selectedPublicationEra == era,
                            onClick = {
                                state = state.copy(selectedPublicationEra = if (state.selectedPublicationEra == era) null else era)
                            },
                            label = { Text(era, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LibraryForestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 6. Minimum Rating Filter
            item {
                Text(
                    text = "MINIMUM RATING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = state.minRating == null,
                        onClick = { state = state.copy(minRating = null) },
                        label = { Text("Any", fontSize = 12.sp) }
                    )
                    ratingOptions.forEach { r ->
                        FilterChip(
                            selected = state.minRating == r,
                            onClick = {
                                state = state.copy(minRating = if (state.minRating == r) null else r)
                            },
                            label = { Text("★ $r+", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LibraryGold,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Apply Button
            item {
                Button(
                    onClick = {
                        onApply(state)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apply_filter_sort_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = LibraryForestGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Apply Filters & Sorting", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
