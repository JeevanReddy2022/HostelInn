package com.example.hostelmanager.ui.payments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.di.appViewModel
import com.example.hostelmanager.ui.theme.AppOnNavy
import com.example.hostelmanager.util.paiseToText
import com.example.hostelmanager.domain.validation.AmountRules
import com.example.hostelmanager.domain.validation.DateRules
import com.example.hostelmanager.ui.components.AppDateField
import com.example.hostelmanager.ui.components.ValidatedTextField
import com.example.hostelmanager.ui.components.appTextFieldColors
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppSoftBlue
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.paiseToRupees
import com.example.hostelmanager.util.rupeesToPaise
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val PENDING_BUFFER_RUPEES = 2000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentScreen(
    hostelId: String,
    prefilledPersonId: String = "",
    prefilledAmountPaise: Long = 0L, // NEW
    onBackClick: () -> Unit,
    viewModel: RecordPaymentViewModel = appViewModel()
) {
    LaunchedEffect(hostelId) { viewModel.setHostelId(hostelId) }
    val allResidents by viewModel.residents.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedResident by remember { mutableStateOf<ResidentWithRoom?>(null) }
    var amount by remember { mutableStateOf("") }
    val paymentModes = listOf("UPI", "Cash", "Bank Transfer")
    var selectedMode by remember { mutableStateOf("UPI") }
    var expandedMode by remember { mutableStateOf(false) }
    var paymentDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var recordedDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var note by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(prefilledPersonId, allResidents) {
        if (prefilledPersonId.isNotEmpty() && prefilledPersonId != "none" && selectedResident == null) {
            selectedResident = allResidents.find { it.person.id == prefilledPersonId }
        }
    }

    val filteredResidents = if (searchQuery.length < 2) {
        emptyList()
    } else {
        allResidents.filter { resident ->
            resident.person.name.contains(searchQuery, ignoreCase = true) ||
                    resident.person.phone.contains(searchQuery) ||
                    resident.roomNumber.contains(searchQuery, ignoreCase = true)
        }.take(5)
    }

    // UPDATED: Uses prefilledAmountPaise if provided
    LaunchedEffect(selectedResident, prefilledAmountPaise) {
        val resident = selectedResident
        if (resident != null && amount.isEmpty()) {
            amount = if (prefilledAmountPaise > 0) {
                prefilledAmountPaise.paiseToRupees().toString()
            } else {
                resident.monthlyRent.paiseToRupees().toString()
            }
        }
    }

    val rentRupees = selectedResident?.monthlyRent?.paiseToRupees() ?: 0L
    val maxAllowed = rentRupees + PENDING_BUFFER_RUPEES
    val amountErr = AmountRules.validateAmount(amount, "Amount", minRupees = 1, maxRupees = if (selectedResident != null) maxAllowed else null)
    val paymentDateErr = DateRules.validateDate(paymentDate, "Payment date", notFuture = true)
    val recordedDateErr = DateRules.validateDate(recordedDate, "Recorded date", notFuture = true)
    val isFormValid = selectedResident != null && amountErr == null && paymentDateErr == null && recordedDateErr == null

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Record Payment", fontWeight = FontWeight.Bold, color = AppText) },
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
            modifier = Modifier.fillMaxSize().padding(padding).imePadding().padding(horizontal = 20.dp, vertical = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedResident == null) {
                ValidatedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    label = "Search resident (name, phone, or room)", error = null,
                    imeAction = ImeAction.Search, keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
                if (filteredResidents.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AppCard), border = BorderStroke(1.dp, AppBorder), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            filteredResidents.forEach { resident ->
                                ResidentSearchResult(resident = resident, onClick = { selectedResident = resident; searchQuery = "" })
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { selectedResident = null; amount = "" },
                    shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = AppSoftBlue), border = BorderStroke(1.dp, AppBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedResident!!.person.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppNavy)
                            Text("Room ${selectedResident!!.roomNumber}", style = MaterialTheme.typography.bodySmall, color = AppMuted)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Rent: ${selectedResident!!.monthlyRent.paiseToText()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppNavy)
                            IconButton(onClick = { selectedResident = null; amount = "" }) { Icon(Icons.Default.Close, contentDescription = "Clear", tint = AppNavy) }
                        }
                    }
                }
            }

            ValidatedTextField(
                value = amount, onValueChange = { input -> amount = input.filter { it.isDigit() } },
                label = "Amount (₹) *", error = amountErr, keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next, keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            AppDateField(label = "Payment Date (when received) *", value = paymentDate, onValueChange = { paymentDate = it }, error = paymentDateErr)
            AppDateField(label = "Recorded Date (when entered) *", value = recordedDate, onValueChange = { recordedDate = it }, error = recordedDateErr)

            ExposedDropdownMenuBox(expanded = expandedMode, onExpandedChange = { expandedMode = !expandedMode }) {
                OutlinedTextField(
                    value = selectedMode, onValueChange = { }, readOnly = true, label = { Text("Payment Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMode) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = appTextFieldColors()
                )
                ExposedDropdownMenu(expanded = expandedMode, onDismissRequest = { expandedMode = false }) {
                    paymentModes.forEach { mode -> DropdownMenuItem(text = { Text(mode) }, onClick = { selectedMode = mode; expandedMode = false }) }
                }
            }

            ValidatedTextField(
                value = note, onValueChange = { note = it }, label = "Note (optional)", error = null,
                imeAction = ImeAction.Done, keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val resident = selectedResident ?: return@Button
                    scope.launch {
                        val success = viewModel.recordPayment(
                            personId = resident.person.id, amountPaise = amount.rupeesToPaise(), mode = selectedMode,
                            paymentDate = paymentDate, recordedDate = recordedDate, note = note.trim()
                        )
                        if (success) onBackClick()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppNavy, contentColor = AppOnNavy, disabledContainerColor = AppBorder, disabledContentColor = AppMuted),
                enabled = isFormValid
            ) { Text("Save Payment", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
        }
    }
}

@Composable
private fun ResidentSearchResult(resident: ResidentWithRoom, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(resident.person.name, fontWeight = FontWeight.Bold, color = AppText)
            Text("Phone: ${resident.person.phone}", style = MaterialTheme.typography.bodySmall, color = AppMuted)
        }
        Text("Room ${resident.roomNumber}", fontWeight = FontWeight.SemiBold, color = AppNavy)
    }
}