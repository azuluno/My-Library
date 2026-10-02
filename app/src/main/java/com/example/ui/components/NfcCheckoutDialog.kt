package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BookEntity
import com.example.data.CheckoutRole
import com.example.data.ReturnDuration
import com.example.ui.theme.LibraryAmber
import com.example.ui.theme.LibraryForestGreen
import com.example.ui.theme.LibraryGold
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcCheckoutDialog(
    initialBook: BookEntity? = null,
    availableBooks: List<BookEntity>,
    onDismiss: () -> Unit,
    onConfirmCheckout: (
        bookTitle: String,
        bookAuthor: String,
        bookCoverUrl: String,
        personName: String,
        role: CheckoutRole,
        returnDurationDays: Int,
        nfcTagId: String,
        notes: String,
        bookId: Long?
    ) -> Unit
) {
    var selectedBook by remember { mutableStateOf(initialBook ?: availableBooks.firstOrNull()) }
    var customTitle by remember { mutableStateOf(initialBook?.title ?: "") }
    var customAuthor by remember { mutableStateOf(initialBook?.author ?: "") }
    var personName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(CheckoutRole.LENT_TO_FRIEND) }
    var selectedDuration by remember { mutableStateOf(ReturnDuration.TWO_WEEKS) }
    var nfcTagId by remember {
        mutableStateOf(initialBook?.nfcTagId ?: "NFC-${(1000..9999).random()}-${('A'..'Z').random()}${('A'..'Z').random()}")
    }
    var notes by remember { mutableStateOf("") }
    var isSimulatingNfcScan by remember { mutableStateOf(false) }
    var nfcScannedSuccess by remember { mutableStateOf(true) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val todayFormatted = remember { dateFormat.format(Date()) }
    val dueCal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, selectedDuration.days)
    }
    val dueFormatted = remember(selectedDuration) { dateFormat.format(dueCal.time) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("nfc_checkout_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
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
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = "NFC Checkout",
                                    tint = LibraryForestGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Neighborhood Checkout",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = LibraryForestGreen
                            )
                            Text(
                                text = "NFC Tag Lending & Tracking",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // NFC Tag Status Box with interactive scan / simulate button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF6F8F6),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE7DC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = LibraryAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Tag: $nfcTagId",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LibraryForestGreen
                                )
                                Text(
                                    text = if (nfcScannedSuccess) "NFC Tag Linked & Ready" else "Ready to Tap",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                nfcTagId = "NFC-${(1000..9999).random()}-${('A'..'Z').random()}${('A'..'Z').random()}"
                                nfcScannedSuccess = true
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-Scan", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Role Selector: Lent to friend vs Borrowed from neighbor
                Text(
                    text = "CHECKOUT TYPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isLent = role == CheckoutRole.LENT_TO_FRIEND
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isLent) LibraryForestGreen else Color(0xFFF1EFEA),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { role = CheckoutRole.LENT_TO_FRIEND }
                            .testTag("role_lent_button")
                    ) {
                        Text(
                            text = "🤝 Lent to Friend",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLent) Color.White else Color(0xFF4A4438),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isLent) LibraryForestGreen else Color(0xFFF1EFEA),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { role = CheckoutRole.BORROWED_FROM_NEIGHBOR }
                            .testTag("role_borrowed_button")
                    ) {
                        Text(
                            text = "📖 Borrowed from Neighbor",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isLent) Color.White else Color(0xFF4A4438),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Book Selection or custom book info
                Text(
                    text = "BOOK INFORMATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (availableBooks.isNotEmpty()) {
                    var expandedDropdown by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedBook?.let { "${it.title} by ${it.author}" } ?: "Select from Library...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Choose Book from Library") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_book_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            availableBooks.forEach { book ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(book.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("by ${book.author}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        selectedBook = book
                                        customTitle = book.title
                                        customAuthor = book.author
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        label = { Text("Book Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customAuthor,
                        onValueChange = { customAuthor = it },
                        label = { Text("Author") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Person's Name
                Text(
                    text = if (role == CheckoutRole.LENT_TO_FRIEND) "FRIEND'S NAME (BORROWER)" else "NEIGHBOR'S NAME (LENDER)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    placeholder = { Text(if (role == CheckoutRole.LENT_TO_FRIEND) "e.g., Sarah Jenkins" else "e.g., David Miller") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("person_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = LibraryForestGreen
                        )
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Dates & Preferred Return Time (7 days, 2 weeks, 1 month, 3 months max)
                Text(
                    text = "PREFERRED RETURN TIME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LibraryForestGreen,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReturnDuration.entries.forEach { duration ->
                        val isSelected = selectedDuration == duration
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) LibraryAmber else Color(0xFFF1EFEA),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedDuration = duration }
                                .testTag("duration_${duration.days}_days")
                        ) {
                            Text(
                                text = duration.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF3E2723) else Color(0xFF4A4438),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date Summary Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFF9E6),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE0A3)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Checkout: $todayFormatted",
                            fontSize = 11.sp,
                            color = Color(0xFF5D4037)
                        )
                        Text(
                            text = "📅 Return By: $dueFormatted",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B4500)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Notes (e.g. lent at book club, handle with care)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Confirm Button
                Button(
                    onClick = {
                        val book = selectedBook
                        val title = book?.title ?: customTitle.ifBlank { "Untitled Book" }
                        val author = book?.author ?: customAuthor.ifBlank { "Unknown Author" }
                        val cover = book?.coverUrl ?: ""
                        val finalPerson = personName.ifBlank { "Neighbor Friend" }
                        onConfirmCheckout(
                            title,
                            author,
                            cover,
                            finalPerson,
                            role,
                            selectedDuration.days,
                            nfcTagId,
                            notes,
                            book?.id
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_nfc_checkout_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LibraryForestGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete Neighborhood Checkout",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
