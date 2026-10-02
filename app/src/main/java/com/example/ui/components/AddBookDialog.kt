package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BookEntity
import com.example.data.BookStatus
import com.example.data.ItemType
import com.example.ui.theme.LibraryForestGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onSaveBook: (BookEntity) -> Unit
) {
    var itemType by remember { mutableStateOf(ItemType.BOOK) }
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var synopsis by remember { mutableStateOf("") }
    var pagesText by remember { mutableStateOf("320") }
    var durationMinsText by remember { mutableStateOf("480") }
    var genre by remember { mutableStateOf("Fiction") }
    var status by remember { mutableStateOf(BookStatus.CURRENTLY_READING) }
    var isCheckedOut by remember { mutableStateOf(false) }
    var checkoutLibrary by remember { mutableStateOf("San Diego Central Library") }
    var dueDate by remember { mutableStateOf("in 14 days") }
    var ratingText by remember { mutableStateOf("4.5") }
    var ratingSource by remember { mutableStateOf("Goodreads") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("add_book_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Add to My Library",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = itemType == ItemType.BOOK,
                        onClick = { itemType = ItemType.BOOK },
                        label = { Text("Physical/E-Book") },
                        leadingIcon = { Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = itemType == ItemType.AUDIOBOOK,
                        onClick = { itemType = ItemType.AUDIOBOOK },
                        label = { Text("Audiobook") },
                        leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Book Title *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("book_title_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Author *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre / Category") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (itemType == ItemType.BOOK) {
                        OutlinedTextField(
                            value = pagesText,
                            onValueChange = { pagesText = it },
                            label = { Text("Total Pages") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        OutlinedTextField(
                            value = durationMinsText,
                            onValueChange = { durationMinsText = it },
                            label = { Text("Total Duration in Minutes (e.g. 480 for 8 hrs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ratingText,
                            onValueChange = { ratingText = it },
                            label = { Text("Rating (1-5)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = ratingSource,
                            onValueChange = { ratingSource = it },
                            label = { Text("Source (Goodreads/Amazon)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = synopsis,
                        onValueChange = { synopsis = it },
                        label = { Text("Synopsis / Overview") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Checked out from library toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isCheckedOut,
                            onCheckedChange = { isCheckedOut = it },
                            modifier = Modifier.testTag("checkout_toggle_checkbox")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Checked out from a library", fontSize = 14.sp)
                    }

                    if (isCheckedOut) {
                        OutlinedTextField(
                            value = checkoutLibrary,
                            onValueChange = { checkoutLibrary = it },
                            label = { Text("Library Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Due Date / Return Timeline") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSaveBook(
                                    BookEntity(
                                        type = itemType.name,
                                        title = title.trim(),
                                        author = author.ifBlank { "Unknown Author" }.trim(),
                                        synopsis = synopsis.trim(),
                                        totalPages = pagesText.toIntOrNull() ?: 300,
                                        totalDurationMinutes = durationMinsText.toIntOrNull() ?: 480,
                                        genre = genre.trim(),
                                        status = status.name,
                                        rating = ratingText.toDoubleOrNull() ?: 4.5,
                                        ratingSource = ratingSource.trim().ifBlank { "Goodreads" },
                                        isCheckedOut = isCheckedOut,
                                        checkoutLibrary = if (isCheckedOut) checkoutLibrary else null,
                                        dueDate = if (isCheckedOut) dueDate else null
                                    )
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LibraryForestGreen),
                        modifier = Modifier.testTag("save_book_button")
                    ) {
                        Text("Add to Shelf")
                    }
                }
            }
        }
    }
}
