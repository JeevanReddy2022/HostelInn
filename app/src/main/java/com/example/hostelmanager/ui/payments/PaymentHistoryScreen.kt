package com.example.hostelmanager.ui.payments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hostelmanager.data.PaymentEdit
import com.example.hostelmanager.data.PaymentWithDetails
import com.example.hostelmanager.di.appViewModel
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppGreen
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppOrange
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppSoftBlue
import com.example.hostelmanager.ui.theme.AppSoftGreen
import com.example.hostelmanager.ui.theme.AppSoftOrange
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.formatDisplayDate
import com.example.hostelmanager.util.formatMonth
import com.example.hostelmanager.util.paiseToText
import com.example.hostelmanager.util.toIsoMonth
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun formatTimestamp(millis: Long): String {
    val zoned = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    return zoned.format(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm:ss a"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    viewModel: PaymentHistoryViewModel = appViewModel()
) {
    LaunchedEffect(hostelId) { viewModel.setHostelId(hostelId) }

    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val residents by viewModel.residents.collectAsStateWithLifecycle()
    val editHistory by viewModel.editHistory.collectAsStateWithLifecycle()

    var selectedMonth by remember { mutableStateOf<String?>(null) }
    var selectedPersonId by remember { mutableStateOf<String?>(null) }
    var expandedMonth by remember { mutableStateOf(false) }
    var personSearchQuery by remember { mutableStateOf("") }
    var paymentToDelete by remember { mutableStateOf<PaymentWithDetails?>(null) }
    var paymentToEdit by remember { mutableStateOf<PaymentWithDetails?>(null) }
    var historyFor by remember { mutableStateOf<PaymentWithDetails?>(null) }

    val months = remember {
        (0 until 12).map { offset -> YearMonth.now().minusMonths(offset.toLong()).toIsoMonth() }
    }

    val filteredResidents = if (personSearchQuery.length < 2) emptyList() else {
        residents.filter { r ->
            r.person.name.contains(personSearchQuery, ignoreCase = true) ||
                    r.person.phone.contains(personSearchQuery) ||
                    r.roomNumber.contains(personSearchQuery, ignoreCase = true)
        }.take(5)
    }

    val selectedPersonName = residents.firstOrNull { it.person.id == selectedPersonId }?.person?.name

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Payment History", fontWeight = FontWeight.Bold, color = AppText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = AppText,
                    navigationIconContentColor = AppText
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedMonth,
                    onExpandedChange = { expandedMonth = !expandedMonth },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedMonth?.let { formatMonth(it) } ?: "All time",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Month", color = AppMuted) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { expandedMonth = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All time") },
                            onClick = {
                                selectedMonth = null
                                viewModel.setMonth(null)
                                expandedMonth = false
                            }
                        )
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(formatMonth(month)) },
                                onClick = {
                                    selectedMonth = month
                                    viewModel.setMonth(month)
                                    expandedMonth = false
                                }
                            )
                        }
                    }
                }

                if (selectedPersonId == null) {
                    OutlinedTextField(
                        value = personSearchQuery,
                        onValueChange = { personSearchQuery = it },
                        label = { Text("Person", color = AppMuted) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search...") }
                    )
                } else {
                    OutlinedTextField(
                        value = selectedPersonName ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Person", color = AppMuted) },
                        trailingIcon = {
                            IconButton(onClick = {
                                selectedPersonId = null
                                viewModel.setPerson(null)
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = AppMuted)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (selectedPersonId == null && filteredResidents.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppCard),
                    border = BorderStroke(1.dp, AppBorder)
                ) {
                    Column {
                        filteredResidents.forEach { resident ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPersonId = resident.person.id
                                        viewModel.setPerson(resident.person.id)
                                        personSearchQuery = ""
                                    }
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        resident.person.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = AppText,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        resident.person.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppMuted
                                    )
                                }
                                Text(
                                    "Room ${resident.roomNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppNavy
                                )
                            }
                        }
                    }
                }
            }

            if (payments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No payments found.",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Adjust filters or record a payment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(payments, key = { it.payment.id }) { detail ->
                        PaymentCard(
                            paymentWithDetails = detail,
                            onEditClick = { paymentToEdit = detail },
                            onHistoryClick = {
                                viewModel.openHistory(detail.payment.id)
                                historyFor = detail
                            },
                            onDeleteClick = { paymentToDelete = detail }
                        )
                    }
                }
            }
        }
    }

    /* ---------------- Edit ---------------- */
    paymentToEdit?.let { detail ->
        EditPaymentDialog(
            payment = detail.payment,
            personName = detail.personName,
            onDismiss = { paymentToEdit = null },
            onSave = { amountPaise, mode, payDate, recDate, note, reason ->
                viewModel.editPayment(
                    paymentId = detail.payment.id,
                    amountPaise = amountPaise,
                    mode = mode,
                    paymentDate = payDate,
                    recordedDate = recDate,
                    note = note,
                    reason = reason
                )
            }
        )
    }

    /* ---------------- Audit history ---------------- */
    historyFor?.let { detail ->
        AlertDialog(
            onDismissRequest = {
                historyFor = null
                viewModel.openHistory(null)
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppText,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = AppOrange)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Edit History", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "${detail.personName} · ${detail.payment.editCount} edit(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted
                    )
                    Text(
                        "Newest first",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (editHistory.isEmpty()) {
                        Text("No edits recorded.", color = AppMuted)
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 380.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(editHistory, key = { _, e -> e.id }) { index, edit ->
                                EditHistoryRow(
                                    edit = edit,
                                    editNumber = editHistory.size - index
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    historyFor = null
                    viewModel.openHistory(null)
                }) { Text("Close", color = AppNavy, fontWeight = FontWeight.Bold) }
            }
        )
    }

    /* ---------------- Delete ---------------- */
    paymentToDelete?.let { detail ->
        val p = detail.payment
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppMuted,
            title = { Text("Delete Payment?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Delete payment of ${p.amountPaise.paiseToText()} from ${detail.personName} " +
                                "on ${formatDisplayDate(p.paymentDate)}?"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Wrong amount? Use Edit instead — deleting also erases the edit history. " +
                                "Delete only for duplicate entries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePayment(p.id)
                    paymentToDelete = null
                }) { Text("Delete", color = AppRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) { Text("Cancel", color = AppMuted) }
            }
        )
    }
}

@Composable
private fun EditHistoryRow(edit: PaymentEdit, editNumber: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppSoftOrange),
        border = BorderStroke(1.dp, AppOrange.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Edit #$editNumber",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppOrange,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    formatTimestamp(edit.editedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (edit.amountBefore != edit.amountAfter) {
                ChangeLine("Amount", edit.amountBefore.paiseToText(), edit.amountAfter.paiseToText())
            }
            if (edit.waivedBefore != edit.waivedAfter) {
                ChangeLine("Forgiven", edit.waivedBefore.paiseToText(), edit.waivedAfter.paiseToText())
            }
            if (edit.modeBefore != edit.modeAfter) {
                ChangeLine("Mode", edit.modeBefore, edit.modeAfter)
            }
            if (edit.paymentDateBefore != edit.paymentDateAfter) {
                ChangeLine(
                    "Received",
                    formatDisplayDate(edit.paymentDateBefore),
                    formatDisplayDate(edit.paymentDateAfter)
                )
            }
            if (edit.recordedDateBefore != edit.recordedDateAfter) {
                ChangeLine(
                    "Recorded",
                    formatDisplayDate(edit.recordedDateBefore),
                    formatDisplayDate(edit.recordedDateAfter)
                )
            }
            if (edit.noteBefore != edit.noteAfter) {
                ChangeLine("Note", edit.noteBefore.ifBlank { "—" }, edit.noteAfter.ifBlank { "—" })
            }

            val explanation = when {
                edit.amountAfter == 0L && edit.waivedAfter > edit.waivedBefore ->
                    "Amount set to ₹0 — the remaining balance was forgiven."
                edit.amountBefore == 0L && edit.amountAfter > 0L ->
                    "Real money recorded — the earlier forgiveness was cancelled."
                else -> null
            }
            if (explanation != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppOrange,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = AppOrange.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Reason: ${edit.reason}",
                style = MaterialTheme.typography.bodySmall,
                color = AppText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ChangeLine(label: String, before: String, after: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = AppMuted,
            modifier = Modifier.width(72.dp)
        )
        Text(
            "$before  →  $after",
            style = MaterialTheme.typography.bodySmall,
            color = AppText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PaymentCard(
    paymentWithDetails: PaymentWithDetails,
    onEditClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val p = paymentWithDetails.payment

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            /* ---------- Left: identity + dates ---------- */
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    paymentWithDetails.personName,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppText,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill("Room ${paymentWithDetails.roomNumber}", AppSoftBlue, AppNavy)
                    Pill("${paymentWithDetails.monthlyRent.paiseToText()}/mo", AppSoftBlue, AppNavy)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Pill("Received: ${formatDisplayDate(p.paymentDate)}", AppSoftGreen, AppGreen)

                if (p.date != p.paymentDate) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Recorded: ${formatDisplayDate(p.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    p.mode,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppNavy,
                    fontWeight = FontWeight.Medium
                )
                if (p.waivedPaise > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Pill("Waived ${p.waivedPaise.paiseToText()}", AppSoftOrange, AppOrange)
                }
                if (p.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        p.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            /* ---------- Right: amount → icons → edited badge ---------- */
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    p.amountPaise.paiseToText(),
                    style = MaterialTheme.typography.titleLarge,
                    color = AppNavy,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = AppNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AppRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // ✅ Edited count now sits BELOW the edit / delete icons
                if (p.editCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppSoftOrange)
                            .clickable { onHistoryClick() }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "View edit history",
                            tint = AppOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Edited ×${p.editCount}",
                            fontSize = 10.sp,
                            color = AppOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String, background: Color, content: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 11.sp, color = content, fontWeight = FontWeight.SemiBold)
    }
}